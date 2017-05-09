package net.dungeonrealms.game.command.dungeon;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kieran Quigley (Proxying) on 20-Jun-16.
 */
public class DungeonJoin extends BaseCommand {
    public DungeonJoin() {
        super("djoin", "/<command>", "Dungeon Join command");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;
        
        Player player = (Player) sender;
        
        if (!GameAPI.isMainWorld(player)) {
        	player.sendMessage(ChatColor.RED + "Dungeons can only be joined from Andalucia.");
        	return true;
        }
        
        if (!Affair.isInParty(player)) {
        	player.sendMessage(ChatColor.RED + "You must be in a party to use this command.");
        	return true;
        }

        Party p = Affair.getParty(player);
        if (!p.isDungeon()) {
        	player.sendMessage(ChatColor.RED + "Your party is not in a dungeon.");
        	return true;
        }
        
        if (!GameAPI.isInSafeRegion(player.getLocation())) {
        	player.sendMessage(ChatColor.RED + "You cannot join a dungeon from this location.");
        	return true;
        }
        
        if (!p.getDungeon().getAllowedPlayers().contains(player)) {
        	player.sendMessage(ChatColor.RED + "You cannot join a dungeon you did not start.");
        	return true;
        }
        
        p.announce(player.getName() + " has joined the dungeon.");
        player.teleport(p.getDungeon().getWorld().getSpawnLocation());
        player.setFallDistance(0F);
        return true;
    }
}
