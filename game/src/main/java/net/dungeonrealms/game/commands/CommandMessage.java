package net.dungeonrealms.game.commands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.player.Rank;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.handlers.FriendHandler;
import net.dungeonrealms.game.punishment.PunishAPI;
import net.dungeonrealms.network.bungeecord.BungeeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Kieran Quigley (Proxying) on 01-Jul-16.
 */
public class CommandMessage extends BasicCommand {

    public CommandMessage(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        if (args.length < 2) {
            return false;
        }

        Player player = (Player) sender;

        if (PunishAPI.isMuted(player.getUniqueId())) {
            player.sendMessage(PunishAPI.getMutedMessage(player.getUniqueId()));
            return true;
        }

        String playerName = args[0];
        String message = String.join(" ", Arrays.asList(args));
        message = message.replace(playerName, "");
        if (DungeonRealms.getInstance().getDevelopers().contains(playerName)) {
            Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.PM_DEV);
        }
        String finalMessage = message;
        String testUUID = DatabaseAPI.getInstance().getUUIDFromName(playerName);
        if (testUUID.equals("")) {
            player.sendMessage(ChatColor.RED + "It seems this user has not played DungeonRealms before.");
            return true;
        }
        UUID uuid = UUID.fromString(testUUID);
        if (!FriendHandler.getInstance().areFriends(player, uuid) && !Rank.getInstance().isGM(Bukkit.getOfflinePlayer(uuid))) {

            if (!(boolean) DatabaseAPI.getInstance().getValue(uuid, EnumData.TOGGLE_RECEIVE_MESSAGE)) {
                player.sendMessage(ChatColor.RED + "This user is only accepting messages from friends.");
                return true;
            }
        }
        player.sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "TO: " + ChatColor.AQUA + playerName + ChatColor.GRAY + ": " + ChatColor.WHITE + finalMessage);
        if (Bukkit.getPlayer(playerName) != null) {
            Bukkit.getOnlinePlayers().stream().filter(player1 -> player1.getName().equalsIgnoreCase(playerName)).limit(1).forEach(theTargetPlayer -> {
                theTargetPlayer.sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "FROM: " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + ": " + ChatColor.WHITE + finalMessage);
                theTargetPlayer.playSound(theTargetPlayer.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
            });
        } else {
            BungeeUtils.sendPlayerMessage(playerName, net.md_5.bungee.api.ChatColor.GRAY.toString() + net.md_5.bungee.api.ChatColor.BOLD + "FROM: " + net.md_5.bungee.api.ChatColor.AQUA + "[" + DungeonRealms.getInstance().shardid + "] " + player.getName() + net.md_5.bungee.api.ChatColor.GRAY + ": " + net.md_5.bungee.api.ChatColor.WHITE + finalMessage);
        }
        return true;
    }
}
