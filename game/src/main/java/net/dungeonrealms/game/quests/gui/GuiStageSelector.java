package net.dungeonrealms.game.quests.gui;

import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.QuestStage;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GuiStageSelector extends GuiBase {

	private Quest quest;
	
	public GuiStageSelector(Player player, Quest quest) {
		super(player, "Stage Selector", 
				( quest.getStageList().size() - 
				( quest.getStageList().size() % 9))
				/ 9 + (quest.getStageList().size() % 9 > 0 ? 2 : 1) );
		this.quest = quest;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void createGUI(){
		//  ACTION BUTTONS  //
		this.setSlot(this.getSize() - 8, Material.WOOL, (short)DyeColor.GREEN.getWoolData(), ChatColor.YELLOW + "Add Stage", new String[] {"Click here to add a new NPC from the bank!"}, (evt) -> {
			QuestStage qs = new QuestStage(this.quest);
			this.quest.getStageList().add(qs);
			new GuiStageEditor(player, qs);
		});
		
		ItemStack moveLeftItem = this.setSlot(this.getSize() - 6, Material.NETHER_BRICK_ITEM, ChatColor.RED + "Left Move Tool", new String[] {"Drag this onto a stage to move it left."}, UNCANCEL);
		
		ItemStack moveRightItem = this.setSlot(this.getSize() - 4, Material.CLAY_BRICK, ChatColor.GREEN + "Right Move Tool", new String[] {"Drag this onto a stage to move it right."}, UNCANCEL);
		
		this.setSlot(this.getSize() - 2, GO_BACK, (evt) -> new GuiQuestEditor(player, quest));
		
		//  QUEST BUTTONS  //
		for(int i = 0; i < this.quest.getStageList().size(); i++){
			QuestStage tempStage = this.quest.getStageList().get(i);
			this.setSlot(i, Material.MAP, ChatColor.GREEN + "Stage " + (i + 1), new String[] {"Left Click to " + ChatColor.GREEN + "Edit" + ChatColor.GRAY + ".", "Right Click to " + ChatColor.RED + "Delete" + ChatColor.GRAY + ".", "Current NPC: " + (tempStage.getNPC() != null ? ChatColor.GREEN + tempStage.getNPC().getName() : ChatColor.RED + "None")}, (evt) -> {
				if(evt.isRightClick()){
					player.sendMessage(ChatColor.RED + "Are you sure you want to remove Stage " + (evt.getRawSlot() + 1) + "?");
					Chat.promptPlayerYesNo(player, (confirm) -> {
						if(confirm){
							player.sendMessage(ChatColor.RED + "Stage " + (evt.getRawSlot() + 1) + " removed.");
							this.quest.getStageList().remove(evt.getRawSlot());
						}
						new GuiStageSelector(player, quest);
					});
					return;
				}
				if(evt.getCursor() != null && evt.getCursor().equals(moveLeftItem)){
					if(evt.getRawSlot() > 0){
						this.quest.getStageList().remove(evt.getRawSlot());
						this.quest.getStageList().add(evt.getRawSlot() - 1, tempStage);
						player.sendMessage(ChatColor.GREEN + "Moved Left.");
					}
					new GuiStageSelector(player, quest);
					return;
				}
				
				if(evt.getCursor() != null && evt.getCursor().equals(moveRightItem)){
					if(this.quest.getStageList().size() > evt.getRawSlot() + 1){
						this.quest.getStageList().remove(evt.getRawSlot());
						this.quest.getStageList().add(evt.getRawSlot() + 1, tempStage);
						player.sendMessage(ChatColor.GREEN + "Moved Right.");
					}
					new GuiStageSelector(player, quest);
					return;
				}
				
				new GuiStageEditor(player, tempStage);
			});
		}
	}
}
