package net.dungeonrealms.common.old.game.database.player.rank;

import net.dungeonrealms.common.old.game.database.DatabaseAPI;
import net.dungeonrealms.common.old.game.database.data.EnumData;
import net.dungeonrealms.common.old.game.database.data.EnumOperators;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.TimeZone;

/**
 * Created by Nick on 9/24/2015.
 */
public class Subscription {

    static Subscription instance = null;

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("American/New_York"));
    }

    public static Subscription getInstance() {
        if (instance == null) {
            instance = new Subscription();
        }
        return instance;
    }

    /**
     * Is used in the startTimer class that checks all players on
     * the server and their subscription time.
     *
     * @param player
     * @since 1.0
     */
    public int checkSubscription(Player player) {
        if (Rank.getInstance().getRank(player.getUniqueId()).equalsIgnoreCase("sub") || Rank.getInstance().getRank(player.getUniqueId()).equalsIgnoreCase("sub+")) {
            int currentTime = (int) (System.currentTimeMillis() / 1000);
            int endTime = (int) DatabaseAPI.getInstance().getData(EnumData.RANK_SUB_EXPIRATION, player.getUniqueId());
            int timeRemaining = (currentTime == 0 || endTime == 0 ? 0 : (endTime - currentTime));
            return (int) (timeRemaining <= 0 ? 0 : Math.ceil(timeRemaining / 86400.0));
        }
        return -1;
    }

    /**
     * Takes PlayerJoinEvent from MainListener
     * and does shit.
     *
     * @param player
     * @since 1.0
     */
    public void handleLogin(Player player) {
        int subLength = checkSubscription(player);
        if (subLength > 0) {
            showSubscriptionExpiry(player, subLength);
        } else if (subLength == 0) {
            expireSubscription(player);
        } else if (subLength == -1 && Rank.getInstance().getRank(player.getUniqueId()).equalsIgnoreCase("sub++")) {
            showSubscriptionExpiry(player, -1);
        }
    }

    public void expireSubscription(Player player) {
        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.RANK, "DEFAULT", true);
        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.RANK_SUB_EXPIRATION, 0, true);
        player.sendMessage(ChatColor.RED + "Your subscription has expired!");
    }

    public void showSubscriptionExpiry(Player player, int daysRemaining) {
        player.sendMessage(ChatColor.GOLD + "You have " + ChatColor.UNDERLINE + (daysRemaining == -1 ? "UNLIMITED" : daysRemaining) + " day" + (daysRemaining != 1 ? "s" : "") + ChatColor.GOLD + " left until your subscription expires.");
    }

}
