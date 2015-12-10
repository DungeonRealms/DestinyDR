package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.world.party.Affair;
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

        if (args.length == 1) {
            if (Bukkit.getPlayer(args[0]) != null && !Bukkit.getPlayer(args[0]).equals(player)) {
                if (Affair.getInstance().isInParty(Bukkit.getPlayer(args[0]))) {
                    player.sendMessage(ChatColor.RED + "That player is already in a party!");
                    return true;
                }
             /*
                Invoker is in party
                 */
                if (Affair.getInstance().isInParty(player)) {
                /*
                Check if player is owner of the party they're in.
                 */
                    if (Affair.getInstance().isOwner(player)) {

                        if(Affair.getInstance().getParty(player).get().getMembers().size() >= 7) {
                            player.sendMessage(ChatColor.RED + "Your party has reached the max player count!");
                            return true;
                        }

                        if (Bukkit.getPlayer(args[0]) != null) {
                            Affair.getInstance().invitePlayer(Bukkit.getPlayer(args[0]), player);
                            player.sendMessage(ChatColor.GREEN + "Invited " + ChatColor.AQUA + args[0] + " " + ChatColor.GREEN + " to your party!");
                        } else {
                            player.sendMessage(ChatColor.RED + "You must specify a player that isn't [NULL]!");
                        }
                    } else if (!Affair.getInstance().isOwner(player)) {
                        player.sendMessage(ChatColor.RED + "You are not the owner!");
                    } else {
                        player.sendMessage(ChatColor.RED + "You are in a party, but don't have rank [Party Owner]!");
                    }
                } else {
                /*
                Invoker isn't in party!
                 */
                    if (Bukkit.getPlayer(args[0]) != null) {
                        Player inviting = Bukkit.getPlayer(args[0]);
                        if (!Affair.getInstance().isInParty(inviting)) {
                            Affair.getInstance().createParty(player);
                            Affair.getInstance().invitePlayer(inviting, player);
                        } else {
                            player.sendMessage(ChatColor.RED + "That player is already in a party!?");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You must specify a player that isn't [NULL]!");
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + "You must specify a player that isn't [NULL]!");
            }

        } else {
            player.sendMessage(ChatColor.RED + "/pinvite <playerName>");
        }
        return false;
    }
}
