package net.dungeonrealms.game.command.friend;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.sql.SQLDatabase;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.FriendHandler;
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

        // No friends on the event shard.
        if (DungeonRealms.getInstance().isEventShard) {
            player.sendMessage(ChatColor.RED + "You cannot accept a friend request on this shard.");
            return false;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Invalid usage! You must type: /accept <name>");
            return false;
        }

        String name = args[0];

        SQLDatabaseAPI.getInstance().getUUIDFromName(name,false, (uuid) -> {
            if(uuid == null) {
                player.sendMessage(ChatColor.RED + "That is not a player.");
                return;
            }
            PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
                if(!wrapper.isPlaying()) {
                    player.sendMessage(ChatColor.RED + "That player is not on any shards.");
                    return;
                }

                if (!FriendHandler.getInstance().isPendingFrom(player.getUniqueId(), uuid)) {
                    player.sendMessage(ChatColor.RED + "You're not pending a request from that user.");
                    return;
                }

                GameAPI.sendNetworkMessage("Friends", "accept:" + " ," + player.getUniqueId().toString() + "," + player.getName() + "," + uuid.toString());
                FriendHandler.getInstance().acceptFriend(player.getUniqueId(), uuid, name);
            });
        });


        return false;
    }

    private boolean isOnline(String playerName) {
        boolean playing = false;
        SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, (uuid) -> {
            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        });

        return false;
    }

    private boolean isPlayer(String player) {
        String uuid = DatabaseAPI.getInstance().getUUIDFromName(player);
        return uuid.equals("") ? false : true;
    }

}
