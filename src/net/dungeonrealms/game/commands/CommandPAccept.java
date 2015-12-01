package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.world.party.Affair;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 11/9/2015.
 */
public class CommandPAccept extends BasicCommand {
    public CommandPAccept(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) return true;

        Player player = (Player) s;

        if (args.length == 0) {
            if (Affair._invitations.containsKey(player) && Affair._invitations.get(player) != null) {
                Affair._invitations.get(player).getMembers().add(player);
                Affair._invitations.remove(player);
                player.sendMessage(ChatColor.GREEN + "You have joined the party!");
            } else {
                player.sendMessage(ChatColor.RED + "You do not have any pending invitations!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You must specify the party owners name, like so; /paccept <partyOwnerName>");
        }

        return false;
    }
}
