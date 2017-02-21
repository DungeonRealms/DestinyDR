package net.dungeonrealms.lobby.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
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
			player.sendMessage(ChatColor.RED + "Usage: /" + label + " <pin>");
			return false;
		}
		
		if(Lobby.getInstance().isLoggedIn(player)){
			player.sendMessage(ChatColor.RED + "You are already logged in.");
			return false;
		}
		
		Object code = DatabaseAPI.getInstance().getData(EnumData.LOGIN_PIN, player.getUniqueId());
		
		if(code == null){
			player.sendMessage(ChatColor.RED + "You do not have a PIN set. Use /setpin <pin>");
			return false;
		}
		
		String loginCode = (String)code;
		
		if(loginCode.equals(args[0])){
			Lobby.getInstance().allowLogin(player, true);
			player.sendMessage(ChatColor.GREEN + "Logged in.");
		}else{
			player.kickPlayer(ChatColor.RED + "Invalid PIN.");
			Lobby.getInstance().getClient().sendNetworkMessage("GMMessage", ChatColor.RED + player.getName() + " entered an invalid login code!");
		}
		return true;
	}

}
