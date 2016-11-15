package net.dungeonrealms.old.game.command.friend;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.common.old.game.database.DatabaseAPI;
import net.dungeonrealms.common.old.game.database.data.EnumData;
import net.dungeonrealms.old.game.handler.FriendHandler;
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
public class AcceptCommand extends BaseCommand {

    public AcceptCommand(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;

        String name = args[0];


        if (!isPlayer(name)) {
            player.sendMessage(ChatColor.RED + "That is not a player.");
            return false;
        }

        if (!isOnline(name)) {
            player.sendMessage(ChatColor.RED + "That player is not on any shards.");
            return false;
        }


        if (!FriendHandler.getInstance().isPendingFrom(player.getUniqueId(), name.toLowerCase())) {
            player.sendMessage(ChatColor.RED + "You're not pending a request from that user.");
            return false;
        }
        UUID uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(name));
        GameAPI.sendNetworkMessage("Friends", "accept:" + " ," + player.getUniqueId().toString() + "," + player.getName() + "," + uuid.toString());
        FriendHandler.getInstance().acceptFriend(player.getUniqueId(), uuid, name);
        return false;
    }

    private boolean isOnline(String playerName) {
        String uuid = DatabaseAPI.getInstance().getUUIDFromName(playerName);
        return Boolean.valueOf(DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, UUID.fromString(uuid)).toString());
    }

    private boolean isPlayer(String player) {
        String uuid = DatabaseAPI.getInstance().getUUIDFromName(player);
        return uuid.equals("") ? false : true;
    }

}
