package net.dungeonrealms.game.command;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.md_5.bungee.api.ChatColor;

/**
 * Allows DEVs to send players to any shard (Useful for sending alts to US-0)
 * 
 * Created February 5th, 2017.
 * @author Kneesnap
 */
public class CommandSend extends BaseCommand {

	public CommandSend(String command, String usage, String description) {
		super(command, usage, description);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!Rank.isDev(sender)) return true;

		if(args.length <= 1){
			sender.sendMessage(ChatColor.RED + "Usage: /drsend <player> <shard> [force]");
			return true;
		}
		
		Player toSend = Bukkit.getPlayer(args[0]);
		ShardInfo sendTo = ShardInfo.getByPseudoName(args[1]);
		
		if(sendTo == null){
			sender.sendMessage(ChatColor.RED + "Shard Not Found!");
			return true;
		}
		
		if(toSend == null){
			//This is unsafe as it doesn't tell the target server that they're sharding.
			//Meaning this could result in a combat log or something similar.
			//However, since this is command is used by devs for testing, that's not a problem.
			if(args.length < 3 || !args[2].equals("force")){
				UUID uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(args[0]));
				if(uuid == null){
					sender.sendMessage(ChatColor.RED + "Player Not Found");
					return true;
				}
				if((boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, uuid)){
					sender.sendMessage(ChatColor.RED + "Player Not Online");
					return true;
				}
				DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.IS_PLAYING, false, true);
			}
			BungeeUtils.sendToServer(args[0], sendTo.getPseudoName());
		}else{
			GameAPI.sendToShard(toSend, sendTo);
		}
		sender.sendMessage(ChatColor.GREEN + "User Sent!");
		
		return true;
	}

}
