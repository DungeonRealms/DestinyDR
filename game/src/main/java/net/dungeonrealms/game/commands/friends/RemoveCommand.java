package net.dungeonrealms.game.commands.friends;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import net.dungeonrealms.game.handlers.FriendHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Created by chase on 7/8/2016.
 */
public class RemoveCommand extends BasicCommand {

    public RemoveCommand(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;

        String name = args[0];


        if (!isPlayer(name) || !isOnline(name)) {
            player.sendMessage(ChatColor.RED + "That is not a player, or that player is not on any shards.");
            return false;
        }
        UUID uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(name));

        if (!FriendHandler.getInstance().areFriends(player, uuid)) {
            player.sendMessage(ChatColor.RED + "You're not friends with that user.");
            return false;
        }


        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.FRIENDS, uuid.toString(), true);
        player.sendMessage(ChatColor.GREEN + "You have deleted " + ChatColor.BOLD + ChatColor.UNDERLINE + name + ChatColor.GREEN + " from your friends list!");
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$PULL, EnumData.FRIENDS, player.getUniqueId().toString(), true);


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
