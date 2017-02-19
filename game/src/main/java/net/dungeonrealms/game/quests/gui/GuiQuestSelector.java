package net.dungeonrealms.game.quests.gui;

import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.Quests;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GuiQuestSelector extends GuiBase {
	
	public GuiQuestSelector(Player player) {
		super(player, "Quest Selector", Quests.getInstance().questStore.getList());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void createGUI(){
		//  ACTION BUTTONS  //
		this.setSlot(this.getSize() - 3, Material.WOOL, DyeColor.GREEN.getWoolData(), ChatColor.GREEN + "Create Quest", new String[] {"Click here to create a new Quest!"}, (evt) -> {
			createQuest();
		});
		
		this.setSlot(this.getSize() - 5, Material.SIGN, ChatColor.YELLOW + "Quest List", new String[] {"Left Click = " + ChatColor.GREEN + "Edit", "Right Click = " + ChatColor.RED + "Delete"});
		
		this.setSlot(this.getSize() - 7, Quests.createSkull("Villager", ChatColor.GOLD + "NPC Bank", new String[] {"Click here to manage all NPCs."}), (evt) -> new GuiNPCBank(player));
		
		//  QUEST BUTTONS  //
		for(int i = 0; i < Quests.getInstance().questStore.getList().size(); i++){
			Quest quest = Quests.getInstance().questStore.getList().get(i);
			this.setSlot(i, Material.INK_SACK, ChatColor.GREEN + quest.getQuestName(), new String[] {"Click here to edit " + ChatColor.GREEN + quest.getQuestName() + ChatColor.GRAY + ".", "Click while holding a " + ChatColor.RED + "Delete Tool" + ChatColor.GRAY + " to delete."}, (evt) -> {
				if(evt.isRightClick()){
					player.sendMessage(ChatColor.YELLOW + "Are you sure you want to delete" + quest.getQuestName());
					Chat.promptPlayerYesNo(player, (confirm) -> {
						if(confirm){
							player.sendMessage(ChatColor.RED + quest.getQuestName() + " deleted.");
							Quests.getInstance().questStore.getList().remove(evt.getRawSlot());
							Quests.getInstance().questStore.delete(quest);
						}
						new GuiQuestSelector(player);
					});
					return;
				}
				new GuiQuestEditor(player, quest);
			});
		}
	}
	
	private void createQuest(){
		player.sendMessage(ChatColor.YELLOW + "Please enter the name of this new quest.");
		Chat.listenForMessage(player, (evt) -> {
			Quest quest = new Quest(evt.getMessage());
			Quests.getInstance().questStore.getList().add(quest);
			new GuiQuestEditor(player, quest);
		}, p -> new GuiQuestSelector(player));
	}
}
