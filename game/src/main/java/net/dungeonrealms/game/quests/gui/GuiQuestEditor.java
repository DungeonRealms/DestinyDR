package net.dungeonrealms.game.quests.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.Quests;
import net.md_5.bungee.api.ChatColor;

public class GuiQuestEditor extends GuiBase {
	
	private Quest quest;
	
	public GuiQuestEditor(Player player, Quest quest){
		super(player, "Editting " + quest.getQuestName() + ":", 1);
		this.quest = quest;
	}
	
	@Override
	public void createGUI(){
		this.setSlot(0, Material.NAME_TAG, ChatColor.GOLD + "Change Quest Name", new String[] {"Click here to change the name of the quest."}, (evt) -> {
			player.sendMessage(ChatColor.YELLOW + "What should this quest be renamed to?");
			Chat.listenForMessage(player, (event) -> {
				Quests.getInstance().questStore.delete(quest);
				quest.setQuestName(event.getMessage());
				new GuiQuestEditor(player, quest);
			}, p -> new GuiQuestEditor(player, quest));
		});
		
		this.setSlot(1, Material.LEVER, ChatColor.GOLD + "Minimum Level", new String[] {"Click here to set the level requirement", "Current Level Min: " + ChatColor.YELLOW + this.quest.getLevelRequirement()}, (evt) -> {
			player.sendMessage(ChatColor.YELLOW + "What should the level requirement be?");
			Chat.listenForNumber(player, 0, 100, (newLvl) -> {
				this.quest.setLevelRequirement(newLvl);
				player.sendMessage(ChatColor.GREEN + "Level Requirement Updated!");
				new GuiQuestEditor(player, quest);
			}, p -> new GuiQuestEditor(player, quest));
		});
		
		this.setSlot(2, Material.EMERALD, ChatColor.GREEN + "Gem Reward", new String[] {"Click here to set the amount of gems gained by", "completing this quest.", "Current: " + ChatColor.GREEN + this.quest.getGemReward() + "g"}, e -> {
			player.sendMessage(ChatColor.YELLOW + "How many Gems should be given?");
			Chat.listenForNumber(player, 0, Integer.MAX_VALUE, (num) -> {
				this.quest.setGemReward(num);
				player.sendMessage(ChatColor.GREEN + "Gem Reward Updated!");
				new GuiQuestEditor(player, quest);
			}, f -> new GuiQuestEditor(player, quest));
		});
		
		this.setSlot(3, Material.EXP_BOTTLE, ChatColor.YELLOW + "XP Reward", new String[] {"Click here to set the amount of XP gained by", "completing this quest.", "Current: " + ChatColor.YELLOW + this.quest.getXPReward() + " XP"}, e -> {
			player.sendMessage(ChatColor.YELLOW + "How much XP should be rewarded?");
			Chat.listenForNumber(player, 0, Integer.MAX_VALUE, (num) -> {
				this.quest.setXPReward(num);
				player.sendMessage(ChatColor.GREEN + "XP Reward Updated!");
				new GuiQuestEditor(player, quest);
			}, f -> new GuiQuestEditor(player, quest));
		});
		
		this.setSlot(5, Material.PUMPKIN_SEEDS, ChatColor.YELLOW + "Edit Stages", 
				new String[] {ChatColor.GRAY + "Click here to edit quest stages."}, (evt) -> new GuiStageSelector(player, quest));
		
		this.setSlot(6, Material.COMPASS, ChatColor.GREEN + "Quest Interval", 
				new String[] {"Click here to change how often players can do this quest.", 
				"Current Interval: " + ChatColor.GOLD + this.quest.getQuestInterval().getDisplayName()}
				, (evt) -> new GuiQuestSetInterval(player, quest));
		
		
		this.setSlot(8, GO_BACK, (evt) -> new GuiQuestSelector(player));
	}
}
