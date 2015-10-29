package net.dungeonrealms.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.commands.generic.BasicCommand;

/**
 * Created by Chase on Oct 28, 2015
 */
public class CommandNigger extends BasicCommand {
	public CommandNigger(String command, String usage, String description) {
		super(command, usage, description);
	}

	public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
		if (s instanceof ConsoleCommandSender)
			return false;
		Player player = (Player) s;
		Bukkit.broadcastMessage("I hate niggers");
		return true;

	}
}
