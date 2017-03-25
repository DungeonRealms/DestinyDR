package net.dungeonrealms.game.quests.gui;

import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.DialogueLine;
import net.dungeonrealms.game.quests.QuestStage;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GuiDialogueSelector extends GuiBase {

	private QuestStage stage;
	
	public GuiDialogueSelector(Player player, QuestStage stage) {
		super(player, "Dialogue Selector", 
				( stage.getDialogue().size() - 
				( stage.getDialogue().size() % 9))
				/ 9 + (stage.getDialogue().size() % 9 > 0 ? 2 : 1) );
		this.stage = stage;
	}
	
	@Override
	protected void createGUI(){
		//  ACTION BUTTONS  //
		this.setSlot(this.getSize() - 8, Material.WOOL, ChatColor.YELLOW + "Create New Line", new String[] {"Click here to create a new Dialogue Line!"}, (evt) -> createDialogue());
		
		ItemStack moveLeftItem = this.setSlot(this.getSize() - 6, Material.NETHER_BRICK_ITEM, ChatColor.RED + "Left Move Tool", new String[] {"Drag this onto a line to move it left."}, UNCANCEL);
		
		ItemStack moveRightItem = this.setSlot(this.getSize() - 4, Material.CLAY_BRICK, ChatColor.GREEN + "Right Move Tool", new String[] {"Drag this onto a line to move it right."}, UNCANCEL);
		
		this.setSlot(this.getSize() - 2, GO_BACK, (evt) -> new GuiStageEditor(player, stage));
		
		//  QUEST BUTTONS  //
		for(int i = 0; i < stage.getDialogue().size(); i++){
			DialogueLine dialogue = stage.getDialogue().get(i);
			
			this.setSlot(i, Material.PAPER, ChatColor.GREEN + "Line " + (i + 1), new String[] {ChatColor.GRAY + "Click here to edit " + ChatColor.GREEN + "Line " + (i + 1), ChatColor.GRAY + "Click while holding a " + ChatColor.RED + "Delete Tool" + ChatColor.GRAY + " to delete.", "Current Text: " + ChatColor.GOLD + (dialogue.getText() != null ? dialogue.getText() : "None")}, (evt) -> {
				if(evt.isRightClick()){
					player.sendMessage(ChatColor.RED + "Are you sure you want to delete Line " + (evt.getRawSlot() + 1) + "?");
					Chat.promptPlayerConfirmation(player, () -> {
						player.sendMessage(ChatColor.RED + "Line " + (evt.getRawSlot() + 1) + " deleted.");
						this.stage.getDialogue().remove(evt.getRawSlot());
						new GuiDialogueSelector(player, stage);
					}, () -> new GuiDialogueSelector(player, stage));
					return;
				}
				
				if(evt.getCursor() != null && evt.getCursor().equals(moveLeftItem)){
					if(evt.getRawSlot() > 0){
						this.stage.getDialogue().remove(evt.getRawSlot());
						this.stage.getDialogue().add(evt.getRawSlot() - 1, dialogue);
						player.sendMessage(ChatColor.GREEN + "Moved Left.");
					}
					new GuiDialogueSelector(player, stage);
					return;
				}
				
				if(evt.getCursor() != null && evt.getCursor().equals(moveRightItem)){
					if(this.stage.getDialogue().size() > evt.getRawSlot() + 1){
						this.stage.getDialogue().remove(evt.getRawSlot());
						this.stage.getDialogue().add(evt.getRawSlot() + 1, dialogue);
						player.sendMessage(ChatColor.GREEN + "Moved Right.");
					}
					new GuiDialogueSelector(player, stage);
					return;
				}
				
				new GuiDialogueEditor(player, stage, dialogue);
			});
		}
	}
	
	private void createDialogue(){
		DialogueLine newLine = new DialogueLine();
		this.stage.addDialogue(newLine);
		new GuiDialogueEditor(player, stage, newLine);
	}
}
