package net.dungeonrealms.database.rank;

import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.TimeZone;
import java.util.UUID;

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
     * @param uuid
     *
     */
    public int checkSubscription(UUID uuid, int expiration) {
        Rank.PlayerRank rank = Rank.getInstance().getPlayerRank(uuid);
        if (rank == Rank.PlayerRank.SUB || rank == Rank.PlayerRank.SUB_PLUS) {
            int currentTime = (int) (System.currentTimeMillis() / 1000);
            int endTime = expiration;
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
    public void handleLogin(Player player, PlayerWrapper wrapper) {
        int subLength = checkSubscription(player.getUniqueId(), wrapper.getRankExpiration());
        if (subLength > 0) {
            showSubscriptionExpiry(player, subLength);
        } else if (subLength == 0) {
            expireSubscription(player, wrapper);
        } else if (subLength == -1 && Rank.getInstance().getRank(player.getUniqueId()).equalsIgnoreCase("sub++")) {
            showSubscriptionExpiry(player, -1);
        }
    }

    public void expireSubscription(Player player, PlayerWrapper wrapper) {
        wrapper.setRank("default");
        wrapper.setRankExpiration(0);
        Rank.getInstance().getCachedRanks().put(player.getUniqueId(), Rank.PlayerRank.DEFAULT);
        SQLDatabaseAPI.getInstance().addQuery(QueryType.SET_RANK, wrapper.getAccountID());
        player.sendMessage(ChatColor.RED + "Your subscription has expired!");
    }

    public void showSubscriptionExpiry(Player player, int daysRemaining) {
        player.sendMessage(ChatColor.GOLD + "You have " + ChatColor.UNDERLINE + (daysRemaining == -1 ? "UNLIMITED" : daysRemaining) + " day" + (daysRemaining != 1 ? "s" : "") + ChatColor.GOLD + " left until your subscription expires.");
    }

}
