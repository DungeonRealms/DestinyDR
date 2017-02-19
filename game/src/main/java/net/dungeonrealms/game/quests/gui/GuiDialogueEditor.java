package net.dungeonrealms.game.quests.gui;

import java.util.Arrays;

import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.DialogueLine;
import net.dungeonrealms.game.quests.QuestStage;
import net.dungeonrealms.game.quests.Quests;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

public class GuiDialogueEditor extends GuiBase {
	
	private QuestStage stage;
	private DialogueLine dialogueLine;
	
	public GuiDialogueEditor(Player player, QuestStage stage, DialogueLine dialogueLine) {
		super(player, "Edit Dialogue", 1);
		this.stage = stage;
		this.dialogueLine = dialogueLine;
	}
	
	
	@Override
	public void createGUI(){
		this.setSlot(0, Material.PAPER, ChatColor.GOLD + "Set Text", new String[] {"Click here to set what the NPC will say.", "Text starting with ^ result in an action message.", "Text starting with ` result in a raw message.", "Current Text: " + ChatColor.GOLD + (this.dialogueLine.getText() != null ? this.dialogueLine.getText() : "None") }, (evt) -> {
			if(evt.isRightClick()){
				this.dialogueLine.setText(null);
				player.sendMessage(ChatColor.RED + "Text Deleted.");
				new GuiDialogueEditor(player, stage, dialogueLine);
				return;
			}
			player.sendMessage(ChatColor.YELLOW + "Please enter the message for this stage.");
			Chat.listenForMessage(player, (msg) -> {
				this.dialogueLine.setText(ChatColor.translateAlternateColorCodes('&', msg.getMessage()));
				player.sendMessage(ChatColor.GREEN + "Dialogue updated to " + ChatColor.YELLOW + this.dialogueLine.getText());
				new GuiDialogueEditor(player, stage, dialogueLine);
			}, p -> new GuiDialogueEditor(player, stage, dialogueLine));
		});
		
		ItemStack potion = new ItemStack(Material.POTION);
		PotionMeta potionMeta = (PotionMeta)potion.getItemMeta();
		this.dialogueLine.getPotionEffects().forEach((pe) -> potionMeta.addCustomEffect(pe, true));
		potionMeta.setDisplayName(ChatColor.DARK_PURPLE + "Set Potion Effects");
		potionMeta.setLore(Arrays.asList(new String[] {ChatColor.GRAY + "Click here to change Potion Effects."}));
		potion.setItemMeta(potionMeta);
		this.setSlot(2, potion, (evt) -> new GuiPotionEditor(player, stage, dialogueLine));
		
		this.setSlot(4, Material.MAP, ChatColor.GREEN + "Set Teleport Location", new String[]{"Set a location the player will be teleported to when this line is run.", "Current Teleport: " + (this.dialogueLine.getTeleportLocation() != null ? ChatColor.GOLD + Quests.getCoords(this.dialogueLine.getTeleportLocation()) : ChatColor.RED + "None")}, (evt) -> {
			if(evt.isRightClick()){
				this.dialogueLine.setTeleportLocation(null);
				player.sendMessage(ChatColor.RED + "Teleport Deleted.");
				new GuiDialogueEditor(player, stage, dialogueLine);
				return;
			}
			player.sendMessage(ChatColor.YELLOW + "Stand where the location should be set and reply \"Yes\".");
			Chat.promptPlayerYesNo(player, (confirm) -> {
				if(confirm){
					this.dialogueLine.setTeleportLocation(player.getLocation().clone());
					player.sendMessage(ChatColor.GREEN + "Set Teleport Location!");
				}
				new GuiDialogueEditor(player, stage, dialogueLine);
			});
		});
		
		this.setSlot(6, Material.STICK, ChatColor.AQUA + "Set Items", new String[] {"Click here to set items to give the player.", "Current Item Count: " + ChatColor.RED + this.dialogueLine.getItems().size()}, (evt) -> new GuiItemSelector(player, stage, dialogueLine.getItems()));
		
		this.setSlot(8, GO_BACK, (evt) -> new GuiDialogueSelector(player, stage));
	}
}
