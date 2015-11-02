package net.dungeonrealms.notice;

import com.mongodb.client.result.UpdateResult;
import net.dungeonrealms.API;
import net.dungeonrealms.core.Callback;
import net.dungeonrealms.handlers.FriendHandler;
import net.dungeonrealms.handlers.MailHandler;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumGuildData;
import net.dungeonrealms.mongo.EnumOperators;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

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
        ArrayList<String> guildInvitations = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.GUILD_INVITES, player.getUniqueId());
        ArrayList<String> mailbox = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MAILBOX, player.getUniqueId());

        if (guildInvitations.size() > 0) {
            for (String s : guildInvitations) {
                String guildName = s.split(",")[0];

                long inviteSent = Long.valueOf(s.split(",")[1]) * 1000;

                long currentTime = System.currentTimeMillis();

                long differenceInTime = currentTime - inviteSent;
                long diffHours = differenceInTime / (60 * 60 * 1000);

                if (24 - diffHours >= 0) {
                    player.sendMessage(ChatColor.YELLOW + "You have been invited to " + ChatColor.GREEN + guildName + ChatColor.YELLOW + " you have " + (24 - diffHours) + " hours to accept!");
                } else {
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.GUILD_INVITES, guildName + "," + inviteSent, true);
                    DatabaseAPI.getInstance().updateGuild(guildName, EnumOperators.$PULL, EnumGuildData.INVITATIONS, player.getUniqueId().toString(), true);
                    player.sendMessage(ChatColor.YELLOW + "Your invite from " + ChatColor.GREEN + guildName + ChatColor.YELLOW + " has expired!");
                }
            }
        }

        if (friendRequests.size() > 0) {
            for (String s : friendRequests) {
                String name = API.getNameFromUUID(s.split(",")[0]);
                long inviteSent = Long.valueOf(s.split(",")[1]) * 1000;
                long currentTime = System.currentTimeMillis();
                long differenceInTime = currentTime - inviteSent;
                long diffHours = differenceInTime / (60 * 60 * 1000);

                if (24 - diffHours <= 0) {
                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.FRIEND_REQUSTS, s, true, new Callback<UpdateResult>(UpdateResult.class) {
                        @Override
                        public void callback(Throwable failCause, UpdateResult result) {
                            if (result.wasAcknowledged()) {
                                FriendHandler.getInstance().sendFriendMessage(player, ChatColor.RED + "Friend request for " + ChatColor.AQUA + name + ChatColor.RED + " has expired!");
                            } else {
                                player.sendMessage(ChatColor.RED + "Unable to remove invalid friend, please report this issue. (error_Notice:friendRequests Removal");
                            }
                        }
                    });
                }

            }

            FriendHandler.getInstance().sendFriendMessage(player, ChatColor.GREEN + "You have " + ChatColor.AQUA + friendRequests.size() + ChatColor.GREEN + " pending friend request!");
        }

        if (mailbox.size() > 0) {
            MailHandler.getInstance().sendMailMessage(player, ChatColor.GREEN + "You have " + ChatColor.AQUA + mailbox.size() + ChatColor.GREEN + " new mail!");
        }
    }

}
