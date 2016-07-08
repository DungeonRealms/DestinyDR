package net.dungeonrealms.game.commands.friends;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.handlers.FriendHandler;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
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
public class AcceptCommand extends BasicCommand {

    public AcceptCommand(String command, String usage, String description, List<String> aliases) {
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

        if (!FriendHandler.getInstance().isPendingFrom(player.getUniqueId(), name.toLowerCase())) {
            player.sendMessage(ChatColor.RED + "You're not pending a request from that user.");
            return false;
        }

        FriendHandler.getInstance().acceptFriend(player.getUniqueId(), name);
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
