package net.dungeonrealms.lobby.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.lobby.Lobby;
import net.md_5.bungee.api.ChatColor;

public class CommandLogin extends BaseCommand {

	public CommandLogin(String command, String usage, String description, List<String> aliases) {
		super(command, usage, description, aliases);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = Bukkit.getPlayer(sender.getName());
		if(player == null || !Rank.isPMOD(player)) return false;
		
		if(args.length == 0){
			sendMessage(player, "Usage: /" + label + " <pin>", ChatColor.RED);
			return false;
		}
		
		if(Lobby.getInstance().isLoggedIn(player)){
			sendMessage(player, "You are already logged in.", ChatColor.RED);
			return false;
		}
		
		return true;
	}

	private void sendMessage(Player player, String message, ChatColor color) {
		player.sendMessage(color + ChatColor.BOLD.toString() + " >> " + color + message);
	}

}
