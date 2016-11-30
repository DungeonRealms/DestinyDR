package net.dungeonrealms.old.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.common.old.game.database.DatabaseAPI;
import net.dungeonrealms.common.old.game.database.data.EnumData;
import net.dungeonrealms.common.old.game.database.data.EnumOperators;
import net.dungeonrealms.common.old.game.database.player.rank.Rank;
import net.dungeonrealms.old.game.handler.ScoreboardHandler;
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
public class CommandSetRank extends BaseCommand {
    public CommandSetRank(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof ConsoleCommandSender) && !(Rank.isGM((Player) sender))) return false;

        String[] ranks = new String[]{"DEV", "HEADGM", "GM", "PMOD", "SUPPORT", "YOUTUBE", "BUILDER", "SUB++", "SUB+", "SUB", "DEFAULT"};

        // If the user isn't a dev and they're at this point, it means they're a GM.
        // We can't allow for SUB ranks because they need more technical execution & that's for a support agent.
        // We can, however, allow them to set new PMODs / remove them.
        // @todo: Tidy this bit up, it's horribly done, but it works... FOR NOW!
        boolean isGM = false;
        boolean isHeadGM = false;
        if (!(sender instanceof ConsoleCommandSender) && !(Rank.isDev((Player) sender))) {
            if (Rank.isHeadGM((Player) sender)) {
                ranks = new String[]{"GM", "PMOD", "BUILDER", "DEFAULT"};
                isHeadGM = true;
            } else {
                ranks = new String[]{"PMOD", "DEFAULT"};
                isGM = true;
            }
        }

        if (args.length >= 2 && Arrays.asList(ranks).contains(args[1].toUpperCase())) {
            try {
                UUID uuid = Bukkit.getPlayer(args[0]) != null && Bukkit.getPlayer(args[0]).getDisplayName().equalsIgnoreCase(args[0]) ? Bukkit.getPlayer(args[0]).getUniqueId() : UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(args[0]));
                String rank = args[1].toUpperCase();

                // Check for any ranks that cannot be revoked by a GM.
                // DEV | GM | SUPPORT | YOUTUBE
                if (isGM) {
                    String currentRank = Rank.getInstance().getRank(uuid).toUpperCase();
                    if (currentRank.equals("DEV") || (currentRank.equals("GM") && !isHeadGM) || currentRank.equals("SUPPORT") || currentRank.equals("YOUTUBE")) {
                        sender.sendMessage(ChatColor.RED + "You can't change the rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0] + ChatColor.RED + " as they're a " + ChatColor.BOLD + ChatColor.UNDERLINE + currentRank + ChatColor.RED + ".");
                        return false;
                    }
                }

                // Always update the database with the new rank.
                GameAPI.submitAsyncCallback(() -> {
                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RANK, rank, false);
                    return true;
                }, result -> {
                    // Only update the server rank if the user is currently logged in.
                    if (Bukkit.getPlayer(args[0]) != null) {
                        Player player = Bukkit.getPlayer(args[0]);
                        Rank.getInstance().setRank(uuid, rank);
                        ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player, ChatColor.WHITE, GameAPI.getGamePlayer(player).getLevel());
                    } else {
                        GameAPI.updatePlayerData(uuid);
                    }
                    sender.sendMessage(ChatColor.GREEN + "Successfully set the rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0] + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + rank + ChatColor.GREEN + ".");
                });

            } catch (IllegalArgumentException ex) {
                // This exception is thrown if the UUID doesn't exist in the database.
                sender.sendMessage(ChatColor.RED + "Invalid player name: " + args[0] + "!");
            }

        } else {
            sender.sendMessage(ChatColor.RED + "Invalid usage: /setrank <name> <rank>");
            sender.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Ranks: " + ChatColor.GREEN + String.join(" | ", Arrays.asList(ranks)));
        }

        return true;
    }

}
