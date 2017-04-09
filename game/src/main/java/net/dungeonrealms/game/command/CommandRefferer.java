package net.dungeonrealms.game.command;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.md_5.bungee.api.ChatColor;

public class CommandRefferer extends BaseCommand {

	public CommandRefferer() {
		super("referrer", "/<command> <name>", "Set the player who refererred you.", Arrays.asList("referrer", "raf"));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player))
			return true;
		
		Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
			Player player = (Player)sender;
			
			if ((Integer)DatabaseAPI.getInstance().getData(EnumData.TIME_PLAYED, player.getUniqueId()) > 60) {
				player.sendMessage(ChatColor.RED + "Only new players can use this command.");
				return;
			}
			
			if (DatabaseAPI.getInstance().getData(EnumData.RAF_REFERRER, player.getUniqueId()) != null) {
				player.sendMessage(ChatColor.RED + "You have already set your referrer.");
				return;
			}
			
			String id = DatabaseAPI.getInstance().getUUIDFromName(args[0]);
			
			if (id == null || id.equals("")) {
	            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + args[0] + ChatColor.RED + " does not exist in our database.");
	            return;
	        }
			
			UUID uuid = UUID.fromString(id);
			
			//Make sure their IP does not match. TODO: Are we keeping this restriction?
			if (DatabaseAPI.getInstance().getData(EnumData.IP_ADDRESS, uuid).equals(player.getAddress().getAddress().getHostAddress())) {
				return;
			}
			
			DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RAF_REFERRER, id , true);
			player.sendMessage(ChatColor.GREEN + "Referrer set. Welcome to DungeonRealms!");
			BungeeUtils.sendPlayerMessage(args[0], ChatColor.GREEN + ChatColor.BOLD.toString() + player.getName() + ChatColor.GREEN + " has been referred by you.");
		});
		
		return true;
	}
}
