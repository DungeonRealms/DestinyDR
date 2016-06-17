package net.dungeonrealms.game.commands;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.game.commands.generic.BasicCommand;

import java.util.Arrays;
import java.util.UUID;


/**
 * Created by Brad on 09/06/2016.
 */
public class CommandSetRank extends BasicCommand{
    public CommandSetRank(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof ConsoleCommandSender) && !(Rank.isDev((Player) sender))) return false;

        String[] ranks = new String[] { "DEV", "GM", "PMOD", "SUPPORT", "YOUTUBE", "BUILDER", "SUB++", "SUB+", "SUB", "DEFAULT" };

        if (args.length >= 2 && Arrays.asList(ranks).contains(args[1].toUpperCase())) {
            try {
                UUID uuid = Bukkit.getPlayer(args[0]) != null ? Bukkit.getPlayer(args[0]).getUniqueId() : UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(args[0]));
                String rank = args[1].toUpperCase();

                // Only update the server rank if the user is currently logged in.
                if (Bukkit.getPlayer(args[0]) != null) {
                    Rank.getInstance().setRank(uuid, rank);
                } else {
                    API.updatePlayerData(uuid);
                }

                // Always update the database with the new rank.
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RANK, rank, true);

                sender.sendMessage(ChatColor.GREEN + "Successfully set the rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0] + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + rank + ChatColor.GREEN + ".");
            } catch (IllegalArgumentException ex) {
                // This exception is thrown if the UUID doesn't exist in the database.
                sender.sendMessage(ChatColor.RED + "Invalid player name: " + args[0] + "!");
            }

        } else {
            sender.sendMessage(ChatColor.RED + "Invalid usage: /setrank <name> <rank>");
            sender.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Ranks: " + ChatColor.GREEN + String.join(" | ", Arrays.asList(ranks)));;
        }

        return true;
    }

}
