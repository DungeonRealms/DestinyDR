package net.dungeonrealms.database.rank;

import lombok.Getter;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.player.rank.Rank.PlayerRank;
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

	@Getter
	private static Subscription instance = new Subscription();

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("American/New_York"));
    }

    /**
     * Is used in the startTimer class that checks all players on
     * the server and their subscription time.
     *
     * @param uuid
     *
     */
    public int checkSubscription(UUID uuid, int expiration) {
        Rank.PlayerRank rank = Rank.getPlayerRank(uuid);
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
        } else if (subLength == -1 && Rank.isSUBPlusPlus(player)) {
            showSubscriptionExpiry(player, -1);
        }
    }

    public void expireSubscription(Player player, PlayerWrapper wrapper) {
        wrapper.setRank(PlayerRank.DEFAULT);
        wrapper.setRankExpiration(0);
        Rank.getCachedRanks().put(player.getUniqueId(), Rank.PlayerRank.DEFAULT);
        SQLDatabaseAPI.getInstance().addQuery(QueryType.SET_RANK, "DEFAULT", wrapper.getAccountID());
        player.sendMessage(ChatColor.RED + "Your subscription has expired!");
    }

    public void showSubscriptionExpiry(Player player, int daysRemaining) {
        player.sendMessage(ChatColor.GOLD + "You have " + ChatColor.UNDERLINE + (daysRemaining == -1 ? "UNLIMITED" : daysRemaining) + " day" + (daysRemaining != 1 ? "s" : "") + ChatColor.GOLD + " left until your subscription expires.");
    }

}
