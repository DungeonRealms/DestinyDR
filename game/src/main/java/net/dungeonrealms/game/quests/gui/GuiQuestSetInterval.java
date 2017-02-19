package net.dungeonrealms.game.quests.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.QuestInterval;
import net.md_5.bungee.api.ChatColor;

public class GuiQuestSetInterval extends GuiBase{
	
	private Quest quest;
	
	public GuiQuestSetInterval(Player player, Quest quest){
		super(player, "Set Interval", InventoryType.HOPPER);
		this.quest = quest;
	}
	
	@Override
	public void createGUI(){
		for(int i = 0; i < QuestInterval.values().length; i++){
			QuestInterval qi = QuestInterval.values()[i];
			this.setSlot(i, Material.LAPIS_BLOCK, ChatColor.GREEN + qi.getDisplayName(), new String[] {"Click here to change the quest interval", "to " + ChatColor.GREEN + qi.getDisplayName() + ChatColor.GRAY + "."}, (evt) -> {
				quest.setQuestInterval(QuestInterval.values()[evt.getRawSlot()]);
				player.sendMessage(ChatColor.GREEN + "Interval Updated to " + qi.getDisplayName() + ".");
				new GuiQuestEditor(this.player, this.quest);
			});
		}
		this.setSlot(4, GO_BACK, (evt) -> new GuiQuestEditor(this.player, this.quest));
	}
}
