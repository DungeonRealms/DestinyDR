package net.dungeonrealms.game.player.notice;

import net.dungeonrealms.API;
import net.dungeonrealms.game.handlers.FriendHandler;
import net.dungeonrealms.game.handlers.MailHandler;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Nick on 10/11/2015.
 */
@SuppressWarnings("unchecked")
public class Notice {

    static Notice instance = null;

    public static Notice getInstance() {
        if (instance == null) {
            instance = new Notice();
        }
        return instance;
    }

    //TODO: Friends, Guilds and clickable acceptance.

    /**
     * This method handles notices per player.
     * E.g. A player logs in and has a guild invite,
     * or a player logs in and has pending friend request.
     *
     * @param player
     * @since 1.0
     */
    public void doLogin(Player player) {

        ArrayList<String> friendRequests = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIEND_REQUSTS, player.getUniqueId());
        ArrayList<String> mailbox = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MAILBOX, player.getUniqueId());

        if (friendRequests.size() > 0) {
            for (String s : friendRequests) {
                String name = API.getNameFromUUID(UUID.fromString(s.split(",")[0]));
                long inviteSent = Long.valueOf(s.split(",")[1]) * 1000;
                long currentTime = System.currentTimeMillis();
                long differenceInTime = currentTime - inviteSent;
                long diffHours = differenceInTime / (60 * 60 * 1000);

                if (24 - diffHours <= 0) {
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.FRIEND_REQUSTS, s, true);
                    FriendHandler.getInstance().sendFriendMessage(player, ChatColor.RED + "Friend request for " + ChatColor.AQUA + name + ChatColor.RED + " has expired!");
                }
            }
            FriendHandler.getInstance().sendFriendMessage(player, ChatColor.GREEN + "You have " + ChatColor.AQUA + friendRequests.size() + ChatColor.GREEN + " pending friend request!");
        }

        if (mailbox.size() > 0)
            MailHandler.getInstance().sendMailMessage(player, ChatColor.GREEN + "You have " + ChatColor.AQUA + mailbox.size() + ChatColor.GREEN + " new mail!");
    }

}
