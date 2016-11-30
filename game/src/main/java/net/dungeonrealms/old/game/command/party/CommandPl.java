package net.dungeonrealms.old.game.command.party;

import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.old.game.party.PartyMechanics;
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
                if (PartyMechanics.getInstance().isInParty(Bukkit.getPlayer(args[0]))) {
                    player.sendMessage(ChatColor.RED + "That player is already in a party!");
                    return true;
                }
             /*
                Invoker is in party
                 */
                if (PartyMechanics.getInstance().isInParty(player)) {
                /*
                Check if player is owner of the party they're in.
                 */
                    if (PartyMechanics.getInstance().isOwner(player)) {

                        if (PartyMechanics.getInstance().getParty(player).get().getMembers().size() >= 7) {
                            player.sendMessage(ChatColor.RED + "Your party has reached the max player count!");
                            return true;
                        }

                        if (Bukkit.getPlayer(args[0]) != null) {
                            PartyMechanics.getInstance().invitePlayer(Bukkit.getPlayer(args[0]), player);
                            player.sendMessage(ChatColor.GREEN + "Invited " + ChatColor.AQUA + args[0] + " " + ChatColor.GREEN + " to your party!");
                        } else {
                            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + args[0] + ChatColor.RED + " is OFFLINE!");
                        }
                    } else {
                        player.sendMessage(new String[]{
                                ChatColor.RED + "You are NOT the leader of your party.",
                                ChatColor.GRAY + "Type " + ChatColor.BOLD + "/pquit" + ChatColor.GRAY + " to quit your current party."
                        });
                    }
                } else {
                /*
                Invoker isn't in party!
                 */
                    if (Bukkit.getPlayer(args[0]) != null) {
                        Player inviting = Bukkit.getPlayer(args[0]);
                        if (!PartyMechanics.getInstance().isInParty(inviting)) {
                            PartyMechanics.getInstance().createParty(player);
                            PartyMechanics.getInstance().invitePlayer(inviting, player);
                        } else {
                            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + args[0] + ChatColor.RED + " is already in your party.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + args[0] + ChatColor.RED + " is OFFLINE!");
                    }
                }
            } else {
                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + args[0] + ChatColor.RED + " is OFFLINE!");
            }

        } else {
            player.sendMessage(new String[]{
                    ChatColor.RED + ChatColor.BOLD.toString() + "Invalid Syntax." + ChatColor.RED + " /plinvite <player>",
                    ChatColor.GRAY + "You can also " + ChatColor.UNDERLINE + "LEFT CLICK" + ChatColor.GRAY + " players with your " + ChatColor.ITALIC + "Character Journal" + ChatColor.GRAY + " to invite them."
            });
        }
        return false;
    }
}
