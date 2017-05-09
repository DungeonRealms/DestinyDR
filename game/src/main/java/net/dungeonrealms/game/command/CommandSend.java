package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.database.PlayerWrapper;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Allows GM+ to send players to any shard (Useful for sending alts to US-0)
 * 
 * Created February 5th, 2017.
 * @author Kneesnap
 */
public class CommandSend extends BaseCommand {

	public CommandSend() {
		super("drsend", "/<command> <player> <shard> [force]", "Sends a player to a shard.");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!Rank.isTrialGM((Player)sender)) return true;

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
		
		if(toSend == null) {
			if(args.length < 3 || !args[2].equals("force")) {
				SQLDatabaseAPI.getInstance().getUUIDFromName(args[0], false, (uuid) -> {
							if(uuid == null){
								sender.sendMessage(ChatColor.RED + "Player Not Found");
								return;
							}

					PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
						if(wrapper.isPlaying()) {
							sender.sendMessage(ChatColor.RED + "Player Not Online");
							return;
						}
						wrapper.setPlayingStatus(false);
						BungeeUtils.sendToServer(args[0], sendTo.getPseudoName());
					});
				});
			}
		}else{
			GameAPI.sendToShard(toSend, sendTo);
		}
		sender.sendMessage(ChatColor.GREEN + "User Sent!");
		
		return true;
	}

}
