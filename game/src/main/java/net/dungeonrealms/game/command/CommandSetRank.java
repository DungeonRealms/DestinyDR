package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.ScoreboardHandler;
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

        String[] ranks = new String[] { "DEV", "HEADGM", "GM", "TRIALGM", "PMOD", "HIDDENMOD", "SUPPORT", "YOUTUBE", "BUILDER", "SUB++", "SUB+", "SUB", "DEFAULT" };

        // If the user isn't a dev and they're at this point, it means they're a GM.
        // We can't allow for SUB ranks because they need more technical execution & that's for a support agent.
        // We can, however, allow them to set new PMODs / remove them.
        // @todo: Tidy this bit up, it's horribly done, but it works... FOR NOW!
        boolean isGM = false;
        boolean isHeadGM = false;
        if (!(sender instanceof ConsoleCommandSender) && !(Rank.isDev((Player) sender))) {
            if (Rank.isHeadGM((Player) sender)) {
                ranks = new String[] { "GM", "TRIALGM", "PMOD", "HIDDENMOD", "BUILDER", "DEFAULT" };
                isHeadGM = true;
            } else {
                ranks = new String[] { "PMOD", "DEFAULT" };
                isGM = true;
            }
        }

        final boolean finalIsGM = isGM;
        final boolean finalIsHeadGM = isHeadGM;

        if (args.length >= 2 && Arrays.asList(ranks).contains(args[1].toUpperCase())) {
                String rank = args[1].toUpperCase();

            SQLDatabaseAPI.getInstance().getUUIDFromName(args[0], false, (uuid) -> {
                if(uuid == null) {
                    sender.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realms!");
                    return;
                }


                PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
                    if(wrapper == null) {
                        sender.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realms!");
                        return;
                    }
                    String currentRank = wrapper.getRank();
                    if(finalIsGM) {
                        if (currentRank.equals("DEV") || currentRank.equals("HEADGM") || (currentRank.equals("GM") && !finalIsHeadGM) || currentRank.equals("SUPPORT") || currentRank.equals("YOUTUBE")) {
                            sender.sendMessage(ChatColor.RED + "You can't change the rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0] + ChatColor.RED + " as they're a " + ChatColor.BOLD + ChatColor.UNDERLINE + currentRank + ChatColor.RED + ".");
                            return;
                        }
                    }

                    if (!rank.toLowerCase().contains("sub") || rank.equalsIgnoreCase("sub++")) {
                        wrapper.setRankExpiration(0);
                    }

                    wrapper.setRank(rank);
                    wrapper.saveData(true, null, (wrappa) -> {
                        sender.sendMessage(ChatColor.GREEN + "Successfully set the rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0] + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + rank + ChatColor.GREEN + ".");
                        Player other = Bukkit.getPlayer(uuid);
                        if(other != null) {
                            Rank.getInstance().setRank(uuid, rank);
                            ScoreboardHandler.getInstance().setPlayerHeadScoreboard(other, ChatColor.WHITE, GameAPI.getGamePlayer(other).getLevel());
                        } else {
                            GameAPI.updatePlayerData(uuid, "rank");
                        }
                    });


                });
            });

        } else {
            sender.sendMessage(ChatColor.RED + "Invalid usage: /setrank <name> <rank>");
            sender.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Ranks: " + ChatColor.GREEN + String.join(" | ", Arrays.asList(ranks)));
        }

        return true;
    }

}
