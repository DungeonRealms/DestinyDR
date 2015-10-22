package net.dungeonrealms.handlers;

import com.mongodb.client.result.UpdateResult;
import net.dungeonrealms.core.Callback;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Nick on 10/22/2015.
 */
public class FriendHandler {

    static FriendHandler instance = null;

    public static FriendHandler getInstance() {
        if (instance == null) {
            instance = new FriendHandler();
        }
        return instance;
    }

    public void sendRequest(Player player, Player friend) {
        if (areFriends(player, friend.getUniqueId())) return;

        DatabaseAPI.getInstance().update(friend.getUniqueId(), EnumOperators.$PUSH, "notices.friendRequest", player.getUniqueId() + "," + (System.currentTimeMillis() / 1000l), true, new Callback<UpdateResult>(UpdateResult.class) {
            @Override
            public void callback(Throwable failCause, UpdateResult result) {
                if (result.wasAcknowledged()) {
                    sendFriendMessage(player, ChatColor.GREEN + "Friend request was successfully sent.");

                    sendFriendMessage(friend, ChatColor.AQUA + player.getName() + ChatColor.GREEN + " sent you a friend request! Check your friend management UI to accept / deny!");
                } else {
                    sendFriendMessage(player, ChatColor.RED + "Unable to process request MatchCount:" + result.getMatchedCount() + " ModifiedCount:" + result.getModifiedCount());
                }
            }
        });

    }

    /**
     * Will check and determine if the players are friends or have a pending
     * friend request.
     *
     * @param player Main player
     * @param uuid   The other player
     * @return
     * @since 1.0
     */
    public boolean areFriends(Player player, UUID uuid) {
        ArrayList<String> friends = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIENDS, player.getUniqueId());

        if (friends.contains(uuid.toString())) {
            return true;
        }

        ArrayList<String> pendingRequest = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIEND_REQUSTS, player.getUniqueId());

        long pendingRequests = pendingRequest.stream().filter(s -> s.startsWith(uuid.toString())).count();

        return pendingRequests >= 1;
    }

    /**
     * simple method to format messages for future.
     *
     * @param player  Player
     * @param message Message
     * @since 1.0
     */
    public void sendFriendMessage(Player player, String message) {
        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "FRIEND" + ChatColor.WHITE + "] " + ChatColor.RESET + message);
    }

}
