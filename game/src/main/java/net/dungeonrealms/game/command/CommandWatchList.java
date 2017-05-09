/*package net.dungeonrealms.game.command;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.anticheat.WatchList;
import net.dungeonrealms.game.mastery.UUIDHelper;
import net.md_5.bungee.api.ChatColor;

public class CommandWatchList extends BaseCommand {

	public CommandWatchList() {
		super("watchlist", "/<command> [add/remove]", "Edit the player watchlist.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) return false;
		if(!Rank.isGM((Player)sender)) return false;
		
		if(args.length == 0) {
			WatchList.openBook((Player)sender);
		} else {
			if(args[0].equals("add")) {
				Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
					UUID uuid = UUIDHelper.getOnlineUUID(args[1]);
					if (uuid != null) {
						WatchList.addPlayer(uuid);
						sender.sendMessage(ChatColor.GREEN + args[1] + " has been added.");
					} else {
						sender.sendMessage(ChatColor.RED + "Player not found.");
					}
				});
			} else if(args[0].equals("remove")) { 
				Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
					UUID uuid = UUIDHelper.getOnlineUUID(args[1]);
					if (uuid != null) {
						WatchList.removePlayer(uuid);
						sender.sendMessage(ChatColor.GREEN + args[1] + " has been removed.");
					} else {
						sender.sendMessage(ChatColor.RED + "Player not found.");
					}
				});
			} else {
				sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
			}
		}
		return true;
	}

}
*/