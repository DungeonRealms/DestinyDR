package net.dungeonrealms.commands;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.party.Affair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 11/9/2015.
 */
public class CommandPl extends BasicCommand {

    public CommandPl(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {


        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        //pinvite <playerName>

        if (args.length == 0) {
            if (Bukkit.getPlayer(args[0]) != null) {
                Player inviting = Bukkit.getPlayer(args[0]);
                //Check if the inviting player is in a party.
                if (!Affair.getInstance().isInParty(inviting)) {
                    /*
                    Check if the player inviting is in a paryt
                    or should we create one and invite the player.
                     */
                    if (Affair.getInstance().isInParty(player)) {
                        //Affair.getInstance().getParty(player).get().invitePlayer(inviting);
                    } else {
                        /*
                        Create the invitor a party because he doesn't have one.
                        And invite the player he wants to invite.
                         */
                        Affair.getInstance().createParty(player);
                        //Affair.getInstance().getParty(player).get().invitePlayer(inviting);
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "That player is already in a party!?");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Unable to find that player online?");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Try /pinvite <playerName>");
        }

        return false;
    }
}
