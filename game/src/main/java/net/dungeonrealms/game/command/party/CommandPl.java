package net.dungeonrealms.game.command.party;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.affair.Affair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 11/9/2015.
 */
public class CommandPl extends BaseCommand {

    public CommandPl() {
        super("pinvite", "/<command> [player]", "Will invite a player to a party, creating one if it doesn't exist.");
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;

        if (args.length == 0) {
        	player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Invalid Syntax." + ChatColor.RED + " /plinvite <player>");
        	player.sendMessage(ChatColor.GRAY + "You can also " + ChatColor.UNDERLINE + "LEFT CLICK" + ChatColor.GRAY + " players with your " + ChatColor.ITALIC + "Character Journal" + ChatColor.GRAY + " to invite them.");
            return true;
        }
        
        Player invite = Bukkit.getPlayer(args[0]);
        if (invite == null) {
        	player.sendMessage(ChatColor.RED + args[0] + " is offline.");
        	return true;
        }

        if(invite == player) return true;

        if (!Affair.isInParty(player))
        	Affair.createParty(player);

        if(Affair.isInParty(player)) {
            player.sendMessage(ChatColor.RED + "This person is already in a party!");
            return true;
        }
        Affair.getParty(player).invite(player, invite);
        return true;
    }
}
