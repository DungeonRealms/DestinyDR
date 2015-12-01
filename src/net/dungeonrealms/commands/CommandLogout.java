package net.dungeonrealms.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.commands.generic.BasicCommand;
import net.md_5.bungee.api.ChatColor;

/**
 * Created by Chase on Nov 18, 2015
 */
public class CommandLogout extends BasicCommand {
	public CommandLogout(String command, String usage, String description) {
		super(command, usage, description);
	}

	@Override
	public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
		if (s instanceof Player) {
			Player player = (Player) s;
			player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Starting Logout...");
			API.handleLogout(player.getUniqueId());
			Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()-> player.kickPlayer(ChatColor.GREEN + "Safely Logged Out!"), 100);
		}
		return true;
	}
}
