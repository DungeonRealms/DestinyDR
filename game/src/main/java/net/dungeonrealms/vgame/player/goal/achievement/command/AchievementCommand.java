package net.dungeonrealms.vgame.player.goal.achievement.command;

import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.common.frontend.rank.EnumPlayerRank;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.player.GamePlayer;
import net.dungeonrealms.vgame.player.goal.achievement.EnumAchievement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Giovanni on 20-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class AchievementCommand extends BaseCommand {
    public AchievementCommand(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (Game.getGame().getMongoConnection().getApi().getPlayer(player.getUniqueId()).getRankData().getRank().hasRank(EnumPlayerRank.DEV)) {
                if (args.length != 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /achievement <add/remove> <player> <id>");
                    player.sendMessage(ChatColor.RED + "Example usage: /achievement remove Vawke 0");
                } else {
                    EnumAchievement achievement = null;
                    Player player1 = null;
                    try {
                        achievement = EnumAchievement.getByID(Integer.valueOf(args[2]));
                        achievement.getId(); // Call the exception on purpose
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + args[1] + ChatColor.RED + " is not a valid achievement ID");
                    }
                    try {
                        player1 = Bukkit.getPlayer(args[1]);
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + args[0] + ChatColor.RED + " is not online");
                    }
                    if (achievement != null && player1 != null) {
                        GamePlayer gamePlayer = Game.getGame().getRegistryHandler().getPlayerRegistry().getMap().get(player1.getUniqueId());
                        if (args[0].equalsIgnoreCase("add")) {
                            if (!gamePlayer.hasAchievement(achievement)) {
                                gamePlayer.getData().getCollectionData().getAchievements().add(achievement.name());
                                player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + achievement.name()
                                        + ChatColor.GREEN + " has been added to " + ChatColor.GREEN.toString() + ChatColor.BOLD + args[1]);
                            } else
                                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + args[1] + ChatColor.RED + " already has the achievement: " + achievement.name());
                        } else if (args[0].equalsIgnoreCase("remove")) {
                            if (gamePlayer.hasAchievement(achievement)) {
                                gamePlayer.getData().getCollectionData().getAchievements().remove(achievement.name());
                                player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + achievement.name()
                                        + ChatColor.GREEN + " has been removed from " + ChatColor.GREEN.toString() + ChatColor.BOLD + args[1]);
                            } else
                                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + args[1] + ChatColor.RED + " does not own: " + achievement.name());
                        }
                    }
                }
            }
        }
        return false;
    }
}
