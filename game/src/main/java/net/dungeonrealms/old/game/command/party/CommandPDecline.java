package net.dungeonrealms.old.game.command.party;

import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.old.game.party.PartyMechanics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kieran Quigley (Proxying) on 01-Jul-16.
 */
public class CommandPDecline extends BaseCommand {
    public CommandPDecline(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) return true;

        Player player = (Player) s;

        if (args.length == 0) {
            if (PartyMechanics._invitations.containsKey(player) && PartyMechanics._invitations.get(player) != null) {
                Player owner = PartyMechanics._invitations.get(player).getOwner();
                PartyMechanics._invitations.remove(player);
                owner.sendMessage(ChatColor.RED + player.getName() + " has declined your party invitation.");
                player.sendMessage(ChatColor.GREEN + "You have declined " + owner.getName() + "(s) party invitation.");
            } else {
                player.sendMessage(ChatColor.RED + "You do not have any pending invitations!");
            }
        }
        return false;
    }
}
