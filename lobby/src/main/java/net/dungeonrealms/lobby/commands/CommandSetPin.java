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

public class CommandSetPin extends BaseCommand {

	public CommandSetPin(String command, String usage, String description, List<String> aliases) {
		super(command, usage, description, aliases);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = Bukkit.getPlayer(sender.getName());
		if(player == null || !Rank.isPMOD(player)) return false;
		Object currentCode = null;
		
		if(currentCode == null){
			//If there is no current pin.
			if(args.length == 0){
				sendMessage(player, "Usage: /" + label + " <pin>", ChatColor.RED);
				return false;
			}
		}else{
			//If there is a current pin.
			if(args.length <= 1){
				sendMessage(player, "Usage: /" + label + " <oldpin> <pin>", ChatColor.RED);
				return false;
			}
		}
		
		String newCode = args[0];
		//If they already have a PIN set.
		if(currentCode != null){
			if(!Lobby.getInstance().isLoggedIn(player)){
				sendMessage(player, "You must login to update your PIN!", ChatColor.RED);
				return false;
			}
			
			String code = (String)currentCode;
			if(!code.equals(args[0])){
				sendMessage(player, "The old PIN you have entered is incorrect!", ChatColor.RED);
				return false;
			}
			
			newCode = args[1];
			sendMessage(player, "Your PIN has been updated!", ChatColor.GREEN);
		} else {
			sendMessage(player, "Your PIN has been set!", ChatColor.GREEN);
		}
//
//		DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.LOGIN_PIN, newCode, true);
		return true;
	}

	private void sendMessage(Player player, String message, ChatColor color) {
		player.sendMessage(color + ChatColor.BOLD.toString() + " >> " + color + message);
	}

}
