package net.dungeonrealms.game.commands.parties;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.world.party.Affair;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Kieran Quigley (Proxying) on 01-Jul-16.
 */
public class CommandPDecline extends BasicCommand {
    public CommandPDecline(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) return true;

        Player player = (Player) s;

        if (args.length == 0) {
            if (Affair._invitations.containsKey(player) && Affair._invitations.get(player) != null) {
                Player owner = Affair._invitations.get(player).getOwner();
                Affair._invitations.remove(player);
                owner.sendMessage(ChatColor.RED + player.getName() + " has declined your party invitation.");
                player.sendMessage(ChatColor.GREEN + "You have declined " + owner.getName() + "(s) party invitation.");
            } else {
                player.sendMessage(ChatColor.RED + "You do not have any pending invitations!");
            }
        }
        return false;
    }
}
