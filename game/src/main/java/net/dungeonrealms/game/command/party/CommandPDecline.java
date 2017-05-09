package net.dungeonrealms.game.command.party;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.affair.Affair;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kieran Quigley (Proxying) on 01-Jul-16.
 */
public class CommandPDecline extends BaseCommand {
    public CommandPDecline() {
        super("pdecline", "/<command>", "Decline a party invitation.");
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) return true;

        Player player = (Player) s;
        
        if (!Affair.getInvitations().containsKey(player)) {
        	player.sendMessage(ChatColor.RED + "You do not have an incoming invitation.");
        	return true;
        }
        
        Affair.getInvitations().get(player).getOwner().sendMessage(ChatColor.RED + player.getName() + " declined your party invitation");
        Affair.getInvitations().remove(player);
        player.sendMessage(ChatColor.RED + "Party invitation declined.");
        return false;
    }
}
