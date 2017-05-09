package net.dungeonrealms.game.command.party;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.Party;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 11/9/2015.
 */
public class CommandPRemove extends BaseCommand {
    public CommandPRemove() {
        super("premove", "/<command>", "Remove player from party.", "pkick");
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        if (!Affair.isInParty(player)) {
            player.sendMessage(ChatColor.RED + "You must be in a party.");
            return true;
        }
        
        if (args.length < 1) {
        	player.sendMessage(ChatColor.RED + "Syntax: /pkick <player>");
        	return true;
        }
        
        Party p = Affair.getParty(player);
        
        if (!p.isOwner(player)) {
        	player.sendMessage(ChatColor.RED + "You are not the party leader!");
        	return true;
        }
        
        Player kick = Bukkit.getPlayer(args[0]);
        if (kick == null) {
        	player.sendMessage(ChatColor.RED + "Player not found.");
        	return true;
        }
        
        p.removePlayer(kick, true);
        return true;
    }
}
