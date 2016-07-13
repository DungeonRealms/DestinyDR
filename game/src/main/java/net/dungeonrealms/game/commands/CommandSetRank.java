package net.dungeonrealms.game.commands;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.player.Rank;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import net.dungeonrealms.game.handlers.ScoreboardHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

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
        if (!(sender instanceof ConsoleCommandSender) && !(Rank.isGM((Player) sender))) return false;

        String[] ranks = new String[] { "DEV", "GM", "PMOD", "SUPPORT", "YOUTUBE", "BUILDER", "SUB++", "SUB+", "SUB", "DEFAULT" };

        // If the user isn't a dev and they're at this point, it means they're a GM.
        // We can't allow for SUB ranks because they need more technical execution & that's for a support agent.
        // We can, however, allow them to set new PMODs / remove them.
        boolean isGM = false;
        if (!(sender instanceof ConsoleCommandSender) && !(Rank.isDev((Player) sender))) {
            ranks = new String[] { "PMOD", "BUILDER", "DEFAULT" };
            isGM = true;
        }

        if (args.length >= 2 && Arrays.asList(ranks).contains(args[1].toUpperCase())) {
            try {
                UUID uuid = Bukkit.getPlayer(args[0]) != null ? Bukkit.getPlayer(args[0]).getUniqueId() : UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(args[0]));
                String rank = args[1].toUpperCase();

                // Check for any ranks that cannot be revoked by a GM.
                // DEV | GM | SUPPORT | YOUTUBE
                if (isGM) {
                    String currentRank = Rank.getInstance().getRank(uuid).toUpperCase();
                    if (currentRank.equals("DEV") || currentRank.equals("GM") || currentRank.equals("SUPPORT") || currentRank.equals("YOUTUBE")) {
                        sender.sendMessage(ChatColor.RED + "You can't change the rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0] + ChatColor.RED + " as they're a " + ChatColor.BOLD + ChatColor.UNDERLINE + currentRank + ChatColor.RED + ".");
                        return false;
                    }
                }

                // Only update the server rank if the user is currently logged in.
                if (Bukkit.getPlayer(args[0]) != null) {
                    Player player = Bukkit.getPlayer(args[0]);
                    Rank.getInstance().setRank(uuid, rank);
                    ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player, ChatColor.WHITE, GameAPI.getGamePlayer(player).getLevel());
                } else {
                    GameAPI.updatePlayerData(uuid);
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
