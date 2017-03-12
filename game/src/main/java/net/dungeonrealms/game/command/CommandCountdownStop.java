package net.dungeonrealms.game.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import net.dungeonrealms.common.game.command.BaseCommand;

public class CommandCountdownStop extends BaseCommand {

	public CommandCountdownStop() {
		super("cdstop", "/<command>", "Stops the current countdown broadcast.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof ConsoleCommandSender)){
			sender.sendMessage(ChatColor.RED + "This command can only be executed from the console.");
			return true;
		}
		CommandCountdown.stopBroadcast();
		sender.sendMessage(ChatColor.GREEN + "Broadcast cancelled.");
		return true;
	}
	
}
