package net.dungeonrealms.game.command.moderation;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.world.realms.Realm;
import net.dungeonrealms.game.world.realms.Realms;
import net.md_5.bungee.api.ChatColor;

/**
 * Realmsee. Loads a user's realm from FTP.
 * @author Kneesnap
 */
public class CommandRealmsee extends BaseCommand {

	public CommandRealmsee() {
		super("realmsee", "/<command> <player>", "Loads a player's realm.");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player) || !Rank.isTrialGM((Player)sender))
			return true;
		
		Player player = (Player)sender;
		
		if(args.length == 0) {
			player.sendMessage(ChatColor.RED + "Syntax: /" + label + " <player>");
			return true;
		}
		
		Block block = ((Player)player).getTargetBlock((Set<Material>)null, 6);
		
		if(block.getType() == Material.AIR) {
			player.sendMessage(ChatColor.RED + "Please face the block you'd like to place this realm portal on.");
			return true;
		}
		
		Bukkit.getScheduler().runTaskAsynchronously(DungeonRealms.getInstance(), () -> {
			String id = DatabaseAPI.getInstance().getUUIDFromName(args[0]);
			
			if (id.equals("")) {
				player.sendMessage(ChatColor.RED + "Player not found in database.");
				return;
			}
			
			UUID uuid = UUID.fromString(id);
			Player owner = Bukkit.getPlayer(uuid);
			
			Realm realm = Realms.getInstance().getOrCreateRealm(uuid);
			
			if(owner != null && owner.isOnline()) {
				if(realm != null && realm.isOpen()) {
					sender.sendMessage(ChatColor.RED + "This user is on this shard and their realm is open already.");
					return;
				}
			} else
				if ((boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, uuid))
					player.sendMessage(ChatColor.RED + "This user is online on another shard. Changes to their realm may not save.");
			
			realm.openPortal(player, block.getLocation());
		});
		
		return true;
	}
}
