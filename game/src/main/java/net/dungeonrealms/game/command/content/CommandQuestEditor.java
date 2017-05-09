package net.dungeonrealms.game.command.content;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.quests.QuestPlayerData;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.gui.GuiQuestSelector;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandQuestEditor extends BaseCommand {
	public CommandQuestEditor() {
		super("quests", "/<command>", "Edit quests");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!Rank.isGM((Player)sender)) return false;
		if(args.length == 0){
			new GuiQuestSelector(Bukkit.getPlayer(sender.getName()));
		}else if(args[0].equals("wipe")){
			if(args.length == 1){
				sender.sendMessage(ChatColor.RED + "Usage: /quests wipe <player>");
				return false;
			}
			Player p = Bukkit.getPlayer(args[1]);
			if(p == null){
				sender.sendMessage(ChatColor.RED + "Player not online.");
				return false;
			}
			Quests.getInstance().playerDataMap.put(p, new QuestPlayerData(p));
			sender.sendMessage(ChatColor.GREEN + "Quest Data Wiped.");
		}else{
			sender.sendMessage(ChatColor.RED + "Unknown Subcommand.");
		}
		return true;
	}
}
