package net.dungeonrealms.game.quests;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.quests.QuestPlayerData.QuestProgress;
import net.dungeonrealms.game.quests.listeners.KillObjectiveListener;
import net.dungeonrealms.game.quests.listeners.NPCListener;
import net.dungeonrealms.game.quests.objectives.ObjectiveGoTo;
import net.dungeonrealms.game.quests.objectives.QuestObjective;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.teleportation.WorldRegion;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class Quests implements GenericMechanic {
	public StoreBase<QuestNPC> npcStore = new StoreBase<QuestNPC>(QuestNPC.class, "npcs");
	public StoreBase<Quest> questStore = new StoreBase<Quest>(Quest.class, "quests");
	
	public HashMap<Player, QuestPlayerData> playerDataMap = new HashMap<Player, QuestPlayerData>();
	private static Quests INSTANCE = new Quests();
	
	public void startInitialization(){
		Bukkit.getPluginManager().registerEvents(new NPCListener(), DungeonRealms.getInstance());
		Bukkit.getPluginManager().registerEvents(new KillObjectiveListener(), DungeonRealms.getInstance());
		npcStore.load();
		questStore.load();
		
		Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), () -> spawnQuestParticles(), 0, 10);
		Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> checkQuestZones(), 0, 40);
		Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> sendActionBar(), 0, 30);
	}
	

	@Override
	public void stopInvocation() {
		if(DungeonRealms.getInstance().isMasterShard){
			questStore.save();
			npcStore.save();
		}
		this.playerDataMap.clear();
	}
	
	private void spawnQuestParticles(){
		for(QuestNPC npc : this.npcStore.getList())
			npc.getLocation().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, npc.getLocation(), 6, 0.5, 1, 0.5);
	}
	
	private void sendActionBar(){
		Bukkit.getOnlinePlayers().forEach(this::updateActionBar);
	}
	
	public void updateActionBar(Player player){
		QuestPlayerData data = this.playerDataMap.get(player);
		if(data == null)
			return;
		List<Quest> quests = data.getCurrentQuests();
		if(quests.isEmpty())
			return;
		Quest current = quests.get(quests.size() - 1);
		QuestProgress qp = data.getQuestProgress(current);
		QuestStage stage = qp.getCurrentStage();
		if(stage == null || stage.getPrevious() == null)
			return;
		String description = stage.getPrevious().getObjective().getTaskDescription(player, stage);
		if(qp.shouldReceiveActionBar() && description != null)
			TitleAPI.sendActionBar(player, ChatColor.DARK_BLUE + description);
	}
	
	private void checkQuestZones(){
		//This is run async so we don't hang the main thread checking this.
		for(Player player : Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()])){
			QuestPlayerData data = this.playerDataMap.get(player);
			if(data == null)
				continue;
			for(Quest quest : this.questStore.getList()){
				if(data.isDoingQuest(quest)){
					QuestStage stage = data.getQuestProgress(quest).getCurrentStage();
					if(stage.getStageTrigger() == Trigger.LOCATION)
						if(((ObjectiveGoTo)stage.getPrevious().getObjective()).isCompleted(player, stage, stage.getNPC()))
							Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> quest.advanceQuest(player));
				}
			}
		}
	}

	public QuestNPC getNPCByName(String name) {
		for(QuestNPC npc : this.npcStore.getList())
			if(npc.getName().equals(name))
				return npc;
		Bukkit.getLogger().warning("FAILED TO LOAD QUEST NPC \"" + name + "\"");
		return null;
	}
	
	public static ItemStack createSkull(String username, String displayName, String[] lore){
		ItemStack skullItem = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
		SkullMeta meta = (SkullMeta) skullItem.getItemMeta();
        meta.setOwner(username);
        meta.setDisplayName(displayName);
        List<String> list = new ArrayList<String>();
        for(String s : lore)
        	list.add(ChatColor.GRAY + s);
        meta.setLore(list);
        skullItem.setItemMeta(meta);
		return skullItem;
	}
	
	public static String getCoords(Location loc){
		return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
	}

	@Override
	public EnumPriority startPriority() {
		return EnumPriority.POPE;
	}
	
	public static Quests getInstance(){
		return INSTANCE;
	}
	
	public void savePlayerToMongo(Player player){
		if(this.playerDataMap.containsKey(player))
			DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.QUEST_DATA,
					this.playerDataMap.get(player).toJSON().toString(), true);
	}
	
	public void handleLogoutEvents(Player player){
		this.playerDataMap.remove(player);
	}
	
	public void handleLogin(Player player){
		Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
			String data = (String)DatabaseAPI.getInstance().getData(EnumData.QUEST_DATA, player.getUniqueId());
			JsonArray object = new JsonParser().parse(data).getAsJsonArray();
			this.playerDataMap.put(player, new QuestPlayerData(player, object));
		});
	}
	
	public static String getRegionDirections(Location loc){
		WorldRegion region = WorldRegion.getByRegionName(GameAPI.getRegionName(loc));
		if(region != null)
			return "in " + region.getDisplayName();
		return "at [" + getCoords(loc) + "]";
	}
	
	public static boolean isEnabled(){
		return DungeonRealms.getInstance().isMasterShard;
	}
	
	public void triggerObjective(Player player, Class<? extends QuestObjective> cls){
		QuestPlayerData pqd = this.playerDataMap.get(player);
		if(pqd != null)
			pqd.triggerObjectives(cls);
	}
}
