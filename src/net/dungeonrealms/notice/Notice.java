package net.dungeonrealms.notice;

import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by Nick on 10/11/2015.
 */
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

        if (guildInvitations.size() > 0) {
            for (String s : guildInvitations) {
                String guildName = s.split(",")[0];

                long inviteSent = Long.valueOf(s.split(",")[1]);

                long hoursLeft = (((System.currentTimeMillis() / 1000l) / inviteSent) / 60) / 60;
                player.sendMessage(ChatColor.YELLOW + "You have been invited to " + ChatColor.GREEN + guildName + ChatColor.YELLOW + " you have " + hoursLeft + " hours to accept!");
            }
        }
    }

}
