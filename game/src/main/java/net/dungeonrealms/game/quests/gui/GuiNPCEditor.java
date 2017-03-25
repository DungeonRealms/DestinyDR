package net.dungeonrealms.game.quests.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.QuestNPC;
import net.dungeonrealms.game.quests.Quests;
import net.md_5.bungee.api.ChatColor;

public class GuiNPCEditor extends GuiBase {
	private QuestNPC npc;
	
	public GuiNPCEditor(Player player, QuestNPC npc){
		super(player, "NPC Editor", 1);
		this.npc = npc;
	}
	
	@Override
	public void createGUI(){
		
		this.setSlot(0, Material.PAPER, ChatColor.BLUE + "Idle Message", new String[] {"What should this NPC say while the player is not in a quest?", "Current: " + ChatColor.BLUE + this.npc.getIdleMessage()}, (evt) -> {
			player.sendMessage(ChatColor.YELLOW + "What should this NPC say while idle?");
			Chat.listenForMessage(player, (event) -> {
				this.npc.setIdleMessage(event.getMessage());
				player.sendMessage(ChatColor.GREEN + "Idle Message Updated.");
				new GuiNPCEditor(player, npc);
			}, p -> new GuiNPCEditor(player, npc));
		});
		
		this.setSlot(1, Material.NAME_TAG, ChatColor.BLUE + "Change Name", new String[] {"Click here to change the name.", "Current Name: " + ChatColor.BLUE + npc.getName()}, (evt) -> {
			player.sendMessage(ChatColor.YELLOW + "What should this NPC's new name be?");
			Chat.listenForMessage(player, (event) -> {
				if(event.getMessage().length() > 16){
					player.sendMessage(ChatColor.RED + "Name cannot be larger than 16 characters.");
					new GuiNPCEditor(player, npc);
					return;
				}
				if(Quests.getInstance().getNPCByName(event.getMessage()) != null){
					player.sendMessage(ChatColor.RED + "There is already an NPC with this name.");
					new GuiNPCEditor(player, npc);
					return;
				}
				Quests.getInstance().npcStore.delete(npc);
				npc.setName(event.getMessage());
				player.sendMessage(ChatColor.GREEN + "Name updated to " + npc.getName() + ".");
				new GuiNPCEditor(player, npc);
			}, p -> new GuiNPCEditor(player, npc));
		});
		
		this.setSlot(2, createNPCSkinSkull(npc), (evt) -> {
			player.sendMessage(ChatColor.YELLOW + "Please enter the username of the skin you'd like to apply.");
			Chat.listenForMessage(player, (event) -> {
				npc.setSkin(event.getMessage());
				player.sendMessage(ChatColor.GREEN + "Skin updated to " + npc.getSkinOwner() + ".");
				new GuiNPCEditor(player, npc);
			}, p -> new GuiNPCEditor(player, npc));
		});
		
		this.setSlot(3, Material.NETHER_STAR, ChatColor.BLUE + "Change Animation", new String[] {"Click here to change the animation.", "Current Animation: " + ChatColor.BLUE + npc.getAnimation().getDisplayName()}, (evt) -> {
			new GuiNPCSetAnimation(player, npc);
		});
		
		this.setSlot(5, Material.ELYTRA, ChatColor.GREEN + "Teleport", new String[] {"Click here to teleport to this NPC."}, (evt) -> {
			Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> player.teleport(npc.getLocation()));
			player.sendMessage(ChatColor.GREEN + "Teleported.");
			player.closeInventory();
		});
		
		this.setSlot(6, Material.CARROT_STICK, ChatColor.GREEN + "Set Location", new String[] {"Click here to move this NPC."}, (evt) -> {
			player.sendMessage(ChatColor.YELLOW + "Please go to the new location and type \"Yes\".");
			Chat.promptPlayerConfirmation(player, () -> {
				npc.setLocation(player.getLocation().clone());
				player.sendMessage(ChatColor.GREEN + "Location Set.");
				new GuiNPCEditor(player, npc);
			}, () -> new GuiNPCEditor(player, npc));
		});
		
		this.setSlot(8, GO_BACK, (evt) -> new GuiQuestSelector(player));
	}
	
	private ItemStack createNPCSkinSkull(QuestNPC npc){
		return Quests.createSkull(npc.getSkinOwner(), ChatColor.BLUE + "Change Skin", new String[] {"Click here to change their skin.", "Current Skin: " + ChatColor.BLUE + npc.getSkinOwner()});
	}
}
