package net.dungeonrealms.game.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.md_5.bungee.api.ChatColor;

public class CommandCountdown extends BaseCommand {
	
	private static int length;
	private static int interval;
	private static String message;
	private static int broadcastTask;
	
	public CommandCountdown() {
		super("cdstart", "/<command> <length> <frequency> <message...>", "Broadcast a message across the network.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof ConsoleCommandSender)){
			sender.sendMessage(ChatColor.RED + "This command can only be executed from the console.");
			return true;
		}
		
		if(message != null) {
			sender.sendMessage(ChatColor.RED + "There is already a message broadcast right now.");
			return true;
		}
		
		if(args.length < 3) {
			sender.sendMessage(ChatColor.RED + "Syntax: /" + label + " <length> <frequency> <message...>");
			sender.sendMessage(ChatColor.RED + "Length and frequency are measured in minutes.");
			return true;
		}
		
		try {
			int length = Integer.parseInt(args[0]);
			int interval = Integer.parseInt(args[1]);

			//  GETS THE BROADCAST MESSAGE  //
			String message = "";
			for(int i = 2; i < args.length; i++)
				message += " " + args[i];
			if(message.length() > 1)
				message = message.substring(1); //TODO: Do we have a method for this anywhere?
			message = ChatColor.translateAlternateColorCodes('&', message);
			
			if(!message.contains("{0}")) {
				sender.sendMessage(ChatColor.RED + "You must include {0} (Timer Variable) inside the message.");
				return true;
			}
			
			//  STARTS THE BROADCAST  //
			startBroadcast(length, interval, message);
			sender.sendMessage(ChatColor.GREEN + "Countdown started.");
			return true;
		} catch(NumberFormatException e) {
			sender.sendMessage(ChatColor.RED + "Invalid Number.");
		}
		
		return false;
	}
	
	
	public static void startBroadcast(int l, int i, String m) {
		//(Add the interval because it is immediately subtracted when a broadcast occurs)
		length = l + i;
		interval = i;
		message = m;
		sendBroadcast();
	}
	
	public static void stopBroadcast() {
		Bukkit.getScheduler().cancelTask(broadcastTask);
		message = null;
		length = 0;
		interval = 0;
	}
	
	private static void sendBroadcast() {
		length -= interval;
		broadcastTask = 0;
		
		if(length > 0){
			GameAPI.sendNetworkMessage("Broadcast", message.replaceAll("\\{0\\}", length + ""));
			broadcastTask = Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), CommandCountdown::sendBroadcast, interval * 20 * 60).getTaskId();
		}
	}
}
