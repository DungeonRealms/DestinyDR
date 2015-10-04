package net.dungeonrealms.commands;

import net.dungeonrealms.party.Party;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class CommandParty implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) {
            return false;
        }

        Player player = (Player) s;

        if (args.length > 0) {
            String command = args[0].toLowerCase();
            switch (command) {
                case "create":
                    Party.getInstance().createParty(player);
                    break;
                case "invite":
                    if (Party.getInstance().isOwnerOfParty(player)) {
                        Party.getInstance().invitePlayer(Bukkit.getPlayer(args[1]), Party.getInstance().getPlayerParty(player));
                    } else {
                        player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "You do not own a party!");
                    }
                    break;
                case "disband":
                    if (Party.getInstance().isOwnerOfParty(player)) {
                        Party.getInstance().disbandParty(Party.getInstance().getPlayerParty(player));
                    } else {
                        player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "You do not own a party!");
                    }
                    break;
                case "kick":
                    if (Party.getInstance().isInParty(Bukkit.getPlayer(args[1]))) {
                        if (Party.getInstance().isOwnerOfParty(player)) {
                            Party.getInstance().kickPlayer(Party.getInstance().getPlayerParty(player), Bukkit.getPlayer(args[1]));
                        } else {
                            player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "You do not own a party!");
                        }
                    } else {
                        player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "That player is not in your party!");
                    }
                    break;
                case "quit":
                    if (Party.getInstance().isInParty(player)) {
                        Party.getInstance().quitParty(player);
                    } else {
                        player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "You are not in a party?");
                    }
                    break;
                case "leave":
                    if (!Party.getInstance().isInParty(player)) {
                        player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "Did you mean, '/party quit' ?");
                    }
                    break;
                case "accept":
                    if (!Party.getInstance().isInParty(player) && Party.getInstance().isInParty(Bukkit.getPlayer(args[1]))) {
                        Party.RawParty party = Party.getInstance().getPlayerParty(Bukkit.getPlayer(args[1]));
                        if (party.getInviting().contains(Bukkit.getPlayer(args[1]))) {
                            party.getMembers().add(player);
                            party.getInviting().remove(player);
                            player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.YELLOW + "You've join " + Bukkit.getPlayer(args[1]).getName() + "'s party!");
                        } else {
                            player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "That party doesn't have you in the request!");
                        }
                    } else {
                        player.sendMessage(org.bukkit.ChatColor.WHITE + "[" + org.bukkit.ChatColor.AQUA.toString() + org.bukkit.ChatColor.BOLD + "PARTY" + org.bukkit.ChatColor.WHITE + "] " + org.bukkit.ChatColor.RED + "You are not in a party!");
                    }
                    break;
                default:
                    player.sendMessage("ERROR DEFAULT CALLED()..");
            }
        } else
            s.sendMessage(ChatColor.RED + "/party <create, invite, kick, disband>");
        return false;
    }

}