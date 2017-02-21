package net.dungeonrealms.lobby.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.lobby.Lobby;
import net.md_5.bungee.api.ChatColor;

public class CommandSetPin extends BaseCommand {

	public CommandSetPin(String command, String usage, String description, List<String> aliases) {
		super(command, usage, description, aliases);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = Bukkit.getPlayer(sender.getName());
		if(player == null || !Rank.isPMOD(player)) return false;
		Object currentCode = DatabaseAPI.getInstance().getData(EnumData.LOGIN_PIN, player.getUniqueId());
		
		if(currentCode == null){
			//If there is no current pin.
			if(args.length == 0){
				player.sendMessage(ChatColor.RED + "Usage: /" + label + " <pin>");
				return false;
			}
		}else{
			//If there is a current pin.
			if(args.length <= 1){
				player.sendMessage(ChatColor.RED + "Usage: /" + label + " <oldpin> <pin>");
				return false;
			}
		}
		
		String newCode = args[0];
		//If they already have a PIN set.
		if(currentCode != null){
			if(!Lobby.getInstance().isLoggedIn(player)){
				player.sendMessage(ChatColor.RED + "You are not logged in yet.");
				return false;
			}
			
			String code = (String)currentCode;
			if(!code.equals(args[0])){
				player.sendMessage(ChatColor.RED + "Wrong password.");
				return false;
			}
			
			newCode = args[1];
		}
		
		player.sendMessage(ChatColor.GREEN + "Pin Updated.");
		DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.LOGIN_PIN, newCode, true);
		return true;
	}

}
