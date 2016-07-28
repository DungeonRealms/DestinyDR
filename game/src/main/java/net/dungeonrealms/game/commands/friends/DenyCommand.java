package net.dungeonrealms.game.commands.friends;

import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.handlers.FriendHandler;
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
public class DenyCommand extends BasicCommand {

    public DenyCommand(String command, String usage, String description, List<String> aliases) {
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

        if (!FriendHandler.getInstance().isPendingFrom(player.getUniqueId(), name)) {
            player.sendMessage(ChatColor.RED + "You're not pending a request from that user.");
            return false;
        }


        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.FRIEND_REQUSTS, uuid.toString(), true);
        player.sendMessage(ChatColor.GREEN + "You have deleted " + ChatColor.BOLD + ChatColor.UNDERLINE + name + ChatColor.GREEN + " friend request!");
        return false;
    }

    private boolean isOnline(String playerName) {
        String uuid = DatabaseAPI.getInstance().getUUIDFromName(playerName);
        return (boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, UUID.fromString(uuid));
    }

    private boolean isPlayer(String player) {
        String uuid = DatabaseAPI.getInstance().getUUIDFromName(player);
        return uuid.equals("") ? false : true;
    }
}
