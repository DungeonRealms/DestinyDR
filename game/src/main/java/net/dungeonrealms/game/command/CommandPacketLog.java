package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.anticheat.PacketLogger;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPacketLog extends BaseCommand {
	
	public CommandPacketLog (String command, String usage, String description) {
		super(command, usage, description);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player && !Rank.isGM((Player)sender)) return true;
		
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Usage: /packetlog <Player> [Time]");
			return true;
		}
		
		int time = Rank.isDev((Player)sender) ? -1 : 60;
		try{
			time = Integer.parseInt(args[1]);
			if(!Rank.isDev((Player)sender) && time > 60)
				time = 60;
		}catch (Exception e) {
			
		}
		
		Player player = Bukkit.getPlayer(args[0]);
		
		if(player == null){
			sender.sendMessage(ChatColor.RED + "Player Not Found.");
			return true;
		}
		
		if(time > 0){
			PacketLogger.INSTANCE.logPlayerTime(player, time);
			sender.sendMessage(ChatColor.AQUA + "Logging " + player.getName() + " for " + time + " seconds.");
			Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
				if(PacketLogger.INSTANCE.isLogging(player))
					sender.sendMessage(ChatColor.AQUA + "Capture complete for " + player.getName() + ".");
			}, (20 * time) - 1);
		}else{
			if(PacketLogger.INSTANCE.isLogging(player)){
				sender.sendMessage(ChatColor.AQUA + "Stopped logging " + player.getName() + ".");
				PacketLogger.INSTANCE.stopLogging(player, "Manually stopped by " + sender.getName());
			}else{
				sender.sendMessage(ChatColor.AQUA + "Started logging " + player.getName() + ".");
				PacketLogger.INSTANCE.startLogging(player);
			}
		}
		return true;
	}
}
