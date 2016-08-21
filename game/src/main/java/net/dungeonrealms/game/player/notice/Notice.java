package net.dungeonrealms.game.player.notice;

import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.handler.MailHandler;
import net.dungeonrealms.game.player.json.JSONMessage;
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
        ArrayList<String> mailbox = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MAILBOX, player.getUniqueId());
        Long vote = (Long) DatabaseAPI.getInstance().getData(EnumData.LAST_VOTE, player.getUniqueId());

        if (mailbox.size() > 0)
            MailHandler.getInstance().sendMailMessage(player, ChatColor.GREEN + "You have " + ChatColor.AQUA + mailbox.size() + ChatColor.GREEN + " new mail!");

        if (vote == null || (System.currentTimeMillis() - vote) >= 86400000) {
            int ecashAmount = 15;
            if (Rank.isSubscriberPlus(player)) ecashAmount = 25;
            else if (Rank.isSubscriber(player)) ecashAmount = 20;

            final JSONMessage message = new JSONMessage("Vote for " + ecashAmount + " ECASH & 5% EXP, click ", ChatColor.YELLOW);
            message.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, "http://minecraftservers.org/vote/174212");
            message.sendToPlayer(player);
        }
    }

}
