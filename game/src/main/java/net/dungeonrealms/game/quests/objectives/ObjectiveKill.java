package net.dungeonrealms.game.quests.objectives;

import java.util.ArrayList;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.QuestPlayerData;
import net.dungeonrealms.game.quests.QuestPlayerData.QuestProgress;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.gui.GuiBase;
import net.dungeonrealms.game.quests.gui.GuiStageEditor;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.Entity;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.gson.JsonObject;

public class ObjectiveKill implements QuestObjective {

	private int amount = 1;
	private int tier = 1;
	private EnumMonster monsterType = EnumMonster.Bandit;
	private String metaName = monsterType.idName + "Killed";
	private QuestStage questStage;
	
	private ArrayList<Player> selectors = new ArrayList<Player>();
	
	@Override
	public boolean isCompleted(Player player, QuestStage stage, QuestNPC currentNPC) {
		if(player.hasMetadata(metaName) && player.getMetadata(metaName).get(0).asInt() >= this.amount){
			player.removeMetadata(metaName, DungeonRealms.getInstance());
			return true;
		}
		return false;
	}

	@Override
	public String getName() {
		return "Kill";
	}

	@Override
	public String getTaskDescription(Player player, QuestStage stage) {
		if(player != null){
			int killed = getKilled(player);
			if(killed > 0)
				return "Kill " + (this.amount - killed) + " more " + this.monsterType.name + ((this.amount - killed) > 1 ? "s" : "") + "!";
		}
		return "Kill " + this.amount + " " + this.monsterType.name + (this.amount > 1 ? "s" : "");
	}
	
	public void setEntityType(EnumMonster monster, int tier){
		this.monsterType = monster;
		this.metaName = monster.idName + "Killed";
		this.tier = tier;
	}

	@Override
	public JsonObject saveJSON() {
		JsonObject o = new JsonObject();
		o.addProperty("amt", this.amount);
		o.addProperty("entityType", this.monsterType.name());
		return o;
	}

	@Override
	public void loadJSON(JsonObject o) {
		this.amount = o.get("amt").getAsInt();
		if(o.has("entityType"))
			this.setEntityType(EnumMonster.valueOf(o.get("entityType").getAsString()), 1);
		if(o.has("tier"))
			this.tier = o.get("tier").getAsInt();
	}
	
	public void handleKill(EntityDeathEvent evt){
		if(evt.getEntity() != null && evt.getEntity().getKiller() != null && evt.getEntity().getKiller() instanceof Player){
			Entity nmsEnt = ((CraftEntity) evt.getEntity()).getHandle();
			if(!(nmsEnt instanceof DRMonster))
				return;
			
			DRMonster monster = (DRMonster)nmsEnt;
			Player killer = (Player)evt.getEntity().getKiller();
			
			System.out.println("Hello from " + evt.getEntity().getEntityId() + " I am " + monster.getEnum());
			
			if(monster.getEnum() == null || this.monsterType != monster.getEnum()){
				System.out.println("Wrong Type");
				return;
			}
			
			QuestPlayerData data = Quests.getInstance().playerDataMap.get(killer);
			QuestProgress progress = data.getQuestProgress(this.questStage.getQuest());
			
			if(progress == null){
				System.out.println("No Progress");
				return;
			}
			
			System.out.println("Player is on: " + (progress.getStageIndex() - 1) );
			System.out.println("I am on " + this.questStage.getQuest().getStageList().indexOf(this.questStage));
			System.out.println("OBJ MATCH 1 = " + (progress.getCurrentStage() == this.questStage));
			System.out.println("OBJ MATCH 2 = " + (progress.getCurrentStage().equals(this.questStage)));
			System.out.println(progress.getCurrentStage());
			System.out.println(this.questStage);
			
			if((progress.getStageIndex() - 1) != this.questStage.getQuest().getStageList().indexOf(this.questStage)){
				System.out.println("Not doing correct part");
				return;
			}
			
			if(monster.getTier(evt.getEntity()) < this.tier){
				System.out.println("Wrong Tier.");
				return;
			}
			
			System.out.println("Adding");
			//Sound
			
			if(killer.hasMetadata(metaName)){
				killer.setMetadata(metaName, new FixedMetadataValue(DungeonRealms.getInstance(), getKilled(killer) + 1));
			}else{
				killer.setMetadata(metaName, new FixedMetadataValue(DungeonRealms.getInstance(), 1));
			}
			
			int count = getKilled(killer);
			if(count == this.amount){
				killer.sendMessage(ChatColor.YELLOW + "You have killed enough " + this.monsterType.name + "s.");
			}else if(count < this.amount){
				killer.sendMessage(ChatColor.YELLOW + "Killed " + count + "/" + this.amount + " " + this.monsterType.name + ( count > 1 ? "s" : "") + ".");
			}
		}
	}
	
	public void handleEntityInteract(PlayerInteractAtEntityEvent evt){
		if(evt.getHand() == EquipmentSlot.OFF_HAND) return;
		if(this.selectors.contains(evt.getPlayer())){
			Entity nmsEnt = ((CraftEntity) evt.getRightClicked()).getHandle();
			if(nmsEnt == null || !(nmsEnt instanceof DRMonster)){
				evt.getPlayer().sendMessage(ChatColor.RED + "This is not a registered monster type.");
				evt.setCancelled(true);
				return;
			}
			
			DRMonster monster = (DRMonster)nmsEnt;
			if(monster.getEnum() == null){
				evt.getPlayer().sendMessage(ChatColor.RED + "This monster doesn't have its type set? Try selecting a different mob of the same type.");
				return;
			}
			
			this.setEntityType(monster.getEnum(), monster.getTier(evt.getRightClicked()));
			this.selectors.remove(evt.getPlayer());
			evt.getPlayer().sendMessage(ChatColor.GREEN + "Entity Type updated to Tier " + this.tier + " " + this.monsterType.name);
		}
	}
	
	public int getKilled(Player player){
		return player.hasMetadata(metaName) ? player.getMetadata(metaName).get(0).asInt() : 0;
	}

	@Override
	public Material getIcon() {
		return Material.DIAMOND_SWORD;
	}
	
	@Override
	public String[] getDescription(){
		return new String[] {"Player kills entities."};
	}

	@Override
	public GuiBase createEditorGUI(Player player, QuestStage stage) {
		return new GuiKillEditor(player, stage, this);
	}
	
	@Override
	public void setQuestStage(QuestStage qs) {
		this.questStage = qs;
	}
	
	public class GuiKillEditor extends GuiBase {
		
		private QuestStage stage;
		private ObjectiveKill objective;
		
		public GuiKillEditor(Player player, QuestStage stage, ObjectiveKill objective) {
			super(player, "Kill Objective", InventoryType.HOPPER);
			this.stage = stage;
			this.objective = objective;
		}
		
		@Override
		public void createGUI(){
			
			this.setSlot(0, Material.EGG, ChatColor.RED + "Entity Type", new String[] {"Set the Entity to be killed", "Current Entity: " + ChatColor.RED + this.objective.monsterType.name}, (evt) -> {
				player.sendMessage(ChatColor.GREEN + "Please right click the entity type you'd like to select.");
				this.objective.selectors.add(player);
				player.closeInventory();
			});
			
			this.setSlot(1, Material.STICK, ChatColor.AQUA + "Entity Tier", new String[] {"Set the tier of this monster: " + ChatColor.LIGHT_PURPLE + this.objective.tier}, (evt) -> {
				Chat.listenForNumber(player, 1, 5, (tier) -> {
					this.objective.tier = tier;
					player.sendMessage(ChatColor.GREEN + "Tier updated to " + tier + ".");
					new GuiKillEditor(player, stage, objective);
				}, p -> new GuiKillEditor(player, stage, objective));
			});
			
			this.setSlot(2, Material.IRON_SWORD, ChatColor.RED + "Amount", new String[] {"Set the minimum amount of killed monsters", "Current: " + ChatColor.RED + this.objective.amount}, (evt) -> {
				player.sendMessage(ChatColor.YELLOW + "What should the kill requirement be?");
				Chat.listenForNumber(player, 1, 1000, (num) -> {
					this.objective.amount = num;
					player.sendMessage(ChatColor.GREEN + "Kill Requirement set to " + this.objective.amount);
					new GuiKillEditor(player, stage, objective);
				}, p -> new GuiKillEditor(player, stage, objective));
			});
			
			this.setSlot(4, GO_BACK, (evt) -> new GuiStageEditor(player, stage));
		}
	}
}
