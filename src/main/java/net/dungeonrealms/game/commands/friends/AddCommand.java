package net.dungeonrealms.game.commands.friends;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.handlers.FriendHandler;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Created by chase on 7/7/2016.
 */
public class AddCommand extends BasicCommand {

    public AddCommand(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;
        if (args.length != 1) {
            player.sendMessage(ChatColor.GRAY + "/add <PLAYER>");
            return false;
        }

        String playerName = args[0];

        if (Bukkit.getPlayer(playerName) != null) {
            Player friend = Bukkit.getPlayer(playerName);
            if (FriendHandler.getInstance().areFriends(player, friend.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You're already friends.");
                return false;
            }
            FriendHandler.getInstance().sendRequest(player, friend);
            return false;
        }


        if (!isPlayer(playerName)) {
            player.sendMessage(ChatColor.RED + "There is no data for a player by that name!");
            return false;
        }

        if (!isOnline(playerName)) {
            player.sendMessage(ChatColor.RED + "That player is not on any shard!");
            return false;

        }


        String uuid = DatabaseAPI.getInstance().getUUIDFromName(playerName);

        FriendHandler.getInstance().sendRequestOverNetwork(player, uuid);

        return false;
    }

    private boolean isOnline(String playerName) {
        String uuid = DatabaseAPI.getInstance().getUUIDFromName(playerName);
        return DatabaseAPI.getInstance().getData(EnumData.CURRENTSERVER, UUID.fromString(uuid)).equals("none") ? false : true;
    }

    private boolean isPlayer(String player) {
        String uuid = DatabaseAPI.getInstance().getUUIDFromName(player);
        return uuid.equals("") ? false : true;
    }
}
