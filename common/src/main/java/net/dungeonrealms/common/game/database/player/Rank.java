package net.dungeonrealms.common.game.database.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class Rank {

    @Getter private static Map<UUID, PlayerRank> cachedRanks = new HashMap<>();

    static {
    	loadRankData();
    }

    private static void loadRankData() {
        SQLDatabaseAPI.getInstance().executeQuery("SELECT users.account_id, rank, users.uuid FROM ranks LEFT JOIN users ON `ranks`.`account_id` = `users`.`account_id` WHERE rank != 'DEFAULT';", rs -> {
            try {
                long start = System.currentTimeMillis();
                while (rs.next()) {
                    int accountID = rs.getInt("account_id");
                    String uuidString = rs.getString("uuid");
                    if (uuidString == null || uuidString.isEmpty()) {
                        System.out.println("Unable to get UUID string from " + accountID);
                        continue;
                    }
                    UUID uuid = UUID.fromString(uuidString);

                    PlayerRank rank = PlayerRank.getFromInternalName(rs.getString("rank"));
                    if (rank != null)
                    	cachedRanks.put(uuid, rank);
                }

                rs.close();
                Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Loaded " + ChatColor.GREEN + cachedRanks.size() + ChatColor.AQUA + " ranks into memory in " + (System.currentTimeMillis() - start) + "ms");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    public static boolean isSUB(OfflinePlayer player) {
    	return getRank(player).isSUB();
    }
    
    public static boolean isSUBPlus(OfflinePlayer player) {
		return getRank(player).isSubPlus();
	}
    
    public static boolean isSUBPlusPlus(OfflinePlayer player) {
    	return getRank(player).isLifetimeSUB();
    }

    public static boolean isDev(CommandSender s) {
    	return s instanceof OfflinePlayer ? isDev((OfflinePlayer)s) : false;
    }
    
    public static boolean isDev(Player player) {
    	return isDev((OfflinePlayer) player);
    }
    
    public static boolean isDev(OfflinePlayer player) {
    	return Constants.DEVELOPERS.contains(player.getName()) && isDev(player.getUniqueId());
    }
    
    public static boolean isDev(UUID uuid) {
    	return getPlayerRank(uuid).isAtLeast(PlayerRank.DEV);
    }

    public static boolean isHeadGM(OfflinePlayer player) {
        return isHeadGM(player.getUniqueId());
    }
    
    public static boolean isHeadGM(UUID uuid) {
    	return getPlayerRank(uuid).isAtLeast(PlayerRank.HEADGM);
    }
    
    public static boolean isGM(OfflinePlayer player) {
    	return isGM(player.getUniqueId());
    }

    public static boolean isGM(UUID uuid) {
    	return getPlayerRank(uuid).isAtLeast(PlayerRank.GM);
    }
    
    public static boolean isTrialGM(OfflinePlayer player) {
    	return isTrialGM(player.getUniqueId());
    }
    
    public static boolean isTrialGM(UUID uuid) {
    	return getPlayerRank(uuid).isAtLeast(PlayerRank.TRIALGM);
    }
    
    public static boolean isSupport(OfflinePlayer player) {
    	return isSupport(player.getUniqueId());
    }
    
    public static boolean isSupport(UUID uuid) {
    	return getPlayerRank(uuid).isAtLeast(PlayerRank.SUPPORT);
    }
    
    public static boolean isPMOD(OfflinePlayer player) {
    	return isPMOD(player.getUniqueId());
    }
    
    public static boolean isPMOD(UUID uuid) {
    	PlayerRank rank = getPlayerRank(uuid);
    	return rank.isAtLeast(PlayerRank.PMOD) || rank == PlayerRank.HIDDEN_MOD;
    }
    
    /**
     * Get a player's rank.
     */
    public static PlayerRank getRank(OfflinePlayer player) {
    	return getPlayerRank(player.getUniqueId());
    }

    /**
     * Get the rank of a UUID.
     */
    public static PlayerRank getPlayerRank(UUID uuid){
        return cachedRanks.containsKey(uuid) ? cachedRanks.get(uuid) : PlayerRank.values()[0];
    }

    /**
     * Sets a players rank.
     *
     * @param uuid
     * @param sRank
     * @since 1.0
     */
    public static void setRank(UUID uuid, String sRank, Consumer<Void> callback) {
        PlayerRank rank = PlayerRank.getFromInternalName(sRank);
        if (rank == null)
        	return; // @todo: Remove RAW_RANKS, replace with the fixed list.
        Player player = Bukkit.getPlayer(uuid);

        cachedRanks.put(uuid, rank);
        SQLDatabaseAPI.getInstance().executeUpdate(set -> {
            if (player != null) {
                player.sendMessage("                 " + ChatColor.YELLOW + "Your rank is now: " + rank.getPrefix());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
            }
            if(callback != null)
                callback.accept(null);
        }, QueryType.UPDATE_RANK.getQuery(sRank, -1, SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid)));
    }

    @AllArgsConstructor
    public enum PlayerRank {
        DEFAULT("", ChatColor.GRAY, "Default"),
        SUB("S", ChatColor.GREEN, "Subscriber"),
        SUB_PLUS("S+", ChatColor.GOLD, "Subscriber+"),
        SUB_PLUS_PLUS("S++",ChatColor.YELLOW, "Subscriber++"),
        BUILDER("BLD", ChatColor.DARK_GREEN, "Builder"),
        YOUTUBER("YOUTUBER", ChatColor.RED, "YouTuber"),
        HIDDEN_MOD("S+", ChatColor.GREEN, "Subscriber"),
        PMOD("PMOD", ChatColor.WHITE, "Player Moderator"),
        TRIALGM("GM", ChatColor.AQUA, "Trial Game Master"),
        GM("GM", ChatColor.AQUA, "Game Master"),
        HEADGM("GM", ChatColor.AQUA, "Head Game Master"),
        SUPPORT("SUPPORT", ChatColor.BLUE, "Support Agent"),
        DEV("DEV", ChatColor.DARK_AQUA, "Developer");

        private String prefix;
        @Getter private ChatColor chatColor;
        private ChatColor prefixColor;
        private String displayPrefix;
        
        PlayerRank(String prefix, ChatColor color, String fullPrefix) {
        	this(prefix, color, color, fullPrefix);
        }
        
        public boolean isSUB() {
        	return isAtLeast(PlayerRank.SUB);
        }
        
        public boolean isSubPlus() {
        	return isAtLeast(PlayerRank.SUB_PLUS);
        }
        
        public boolean isLifetimeSUB() {
        	return isAtLeast(PlayerRank.SUB_PLUS_PLUS);
        }
        
        public int getRank() {
        	return ordinal();
        }
        
        public String getInternalName() {
        	return name().toLowerCase();
        }
        
        /**
         * Get the prefix that displays in chat.
         * @return
         */
        public String getChatPrefix() {
        	return this.prefix.length() > 0 ? getPrefix() + " " : "";
        }
        
        /**
         * Gets this rank's prefix without an added .
         * @return
         */
        public String getPrefix() {
        	return this.prefixColor + "" + ChatColor.BOLD + this.prefix + ChatColor.RESET;
        }
        
        /**
         * Get the full display prefix.
         */
        public String getFullPrefix() {
        	return getChatColor() + this.displayPrefix;
        }

        public boolean isAtLeast(PlayerRank rank){
            return getRank() >= rank.getRank();
        }
        
        public static PlayerRank getFromInternalName(String name) {
            return Arrays.stream(values()).filter(rank -> rank.getInternalName().equals(name.toLowerCase())).findFirst().orElse(null);
        }
        
        public static PlayerRank getFromPrefix(String prefix) {
        	for (PlayerRank p : values())
        		if (p.getPrefix().equalsIgnoreCase(prefix))
        			return p;
        	return getFromInternalName(prefix);
        }
    }
}