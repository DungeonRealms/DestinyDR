package net.dungeonrealms.game.player.notice;

import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.type.EnumData;
import net.dungeonrealms.game.handlers.MailHandler;
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
     * This type handles notices per player.
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
        }

        if (mailbox.size() > 0)
            MailHandler.getInstance().sendMailMessage(player, ChatColor.GREEN + "You have " + ChatColor.AQUA + mailbox.size() + ChatColor.GREEN + " new mail!");
    }

}
