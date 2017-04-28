package net.dungeonrealms.common.game.database.player.rank;

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
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class Rank {

    static Rank instance = null;

    public static Rank getInstance() {
        if (instance == null) {
            instance = new Rank();
            instance.loadRankData();
        }
        return instance;
    }


//    public static boolean isRank(UUID uuid, Consumer<Rank> callback){
//        callback.accept(rank);
//    }


    @Getter
    private Map<UUID, PlayerRank> cachedRanks = new HashMap<>();

    public void loadRankData() {
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
                    if (rank == null) continue;
                    this.cachedRanks.put(uuid, rank);
                }

                rs.close();
                Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Loaded " + ChatColor.GREEN + this.cachedRanks.size() + ChatColor.AQUA + " Ranks into memory in " + (System.currentTimeMillis() - start) + "ms");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Returns true if user has the rank "dev".
     *
     * @param player
     * @return boolean
     * @todo: Remove "DEV" rank, use "GM" rank and check if in getDevelopers array in the DungeonRealms class.
     */
    public static boolean isDev(OfflinePlayer player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank.equalsIgnoreCase("dev") && Arrays.asList(Constants.DEVELOPERS).contains(player.getName());
    }

    public static boolean isDev(CommandSender commandSender) {
        return commandSender instanceof ConsoleCommandSender || (commandSender instanceof Player && Rank.isDev(((OfflinePlayer) commandSender)));
    }

    public static boolean isDev(Player player) {//This is for legacy purposes.
        return isDev((OfflinePlayer) player);
    }

    /**
     * Returns true if the user has the rank "dev" or "headgm".
     *
     * @param player
     * @return boolean
     */
    public static boolean isHeadGM(OfflinePlayer player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank.equalsIgnoreCase("headgm") || rank.equalsIgnoreCase("dev");
    }

    /**
     * Returns true if the user has the rank "dev" or "gm".
     *
     * @param player
     * @return boolean
     */
    public static boolean isGM(OfflinePlayer player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return isGMRank(rank);
    }

    /**
     * Return true if the user is a Trial GM or higher.
     *
     * @param player
     * @return
     */
    public static boolean isTrialGM(OfflinePlayer player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank.equalsIgnoreCase("trialgm") || rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("headgm") || rank.equalsIgnoreCase("dev");
    }

    public static boolean isGMRank(String rank) {
        return rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("headgm") || rank.equalsIgnoreCase("dev");
    }

    public static boolean isTrialGMRank(String rank) {
        return rank.equalsIgnoreCase("trialgm") || rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("headgm") || rank.equalsIgnoreCase("dev");
    }

    /**
     * Returns true if the user has the rank "dev" or "support".
     *
     * @param player
     * @return boolean
     */
    public static boolean isSupport(OfflinePlayer player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank.equalsIgnoreCase("support") || rank.equalsIgnoreCase("dev");
    }

    /**
     * Returns true if the user has the rank "dev", "gm" or "pmod".
     *
     * @param player
     * @return boolean
     */
    public static boolean isPMOD(OfflinePlayer player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return isAtleastPMOD(rank);
    }

    public static boolean isAtleastPMOD(String rank) {
        return rank.equalsIgnoreCase("hiddenmod") || rank.equalsIgnoreCase("pmod") || rank.equalsIgnoreCase("support") || rank.equalsIgnoreCase("trialgm") || rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("headgm") || rank.equalsIgnoreCase("dev");
    }

    /**
     * Returns true if the user has the rank "dev", "gm", "pmod" or "youtube".
     *
     * @param player
     * @return boolean
     */
    public static boolean isYouTuber(OfflinePlayer player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank.equalsIgnoreCase("youtube") || rank.equalsIgnoreCase("trialgm") || rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("headgm") || rank.equalsIgnoreCase("dev");
    }

    public static boolean isBuilder(OfflinePlayer player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank.equalsIgnoreCase("builder") || isGM(player);
    }

    /**
     * Returns true if the user does not have the "default" rank.
     *
     * @param player
     * @return boolean
     */
    public static boolean isSubscriber(Player player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank != null && !rank.equalsIgnoreCase("default");
    }

    public static boolean isSubscriberPlus(Player player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank != null && !rank.equalsIgnoreCase("default") && !rank.equalsIgnoreCase("sub") && !rank.equalsIgnoreCase("hiddenmod");
    }

    public static boolean isSubscriberLifetime(Player player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank != null && !rank.equalsIgnoreCase("default") && !rank.equalsIgnoreCase("sub") && !rank.equalsIgnoreCase("sub+") && !rank.equalsIgnoreCase("hiddenmod");
    }

    /**
     * Gets the players rank.
     *
     * @param uuid
     * @return
     * @since 1.0
     */
    public String getRank(UUID uuid) {
        PlayerRank rank = this.cachedRanks.get(uuid);

        return (rank == null ? "default" : rank.getInternalName()).toUpperCase();
    }

    public PlayerRank getPlayerRank(UUID uuid){
        PlayerRank rank = this.cachedRanks.get(uuid);
        if(rank == null)return PlayerRank.DEFAULT;
        return rank;
    }

    /**
     * Sets a players rank.
     *
     * @param uuid
     * @param sRank
     * @since 1.0
     */
    public void setRank(UUID uuid, String sRank, Consumer<Void> callback) {
        PlayerRank rank = PlayerRank.getFromInternalName(sRank);
        if (rank == null) return; // @todo: Remove RAW_RANKS, replace with the fixed list.
        Player player = Bukkit.getPlayer(uuid);

        this.cachedRanks.put(uuid, rank);
        SQLDatabaseAPI.getInstance().executeUpdate(set -> {
            if (player != null) {
                player.sendMessage("                 " + ChatColor.YELLOW + "Your rank is now: " + rank.getPrefix());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
            }
            if(callback != null)
                callback.accept(null);
        }, QueryType.UPDATE_RANK.getQuery(sRank, -1, SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid)));

    }


    /*
     case "dev":
                return ChatColor.AQUA;
            case "headgm":
            case "gm":
            case "trialgm":
                return ChatColor.AQUA;
            case "pmod":
                return ChatColor.WHITE;
            case "support":
                return ChatColor.BLUE;
            case "youtube":
                return ChatColor.RED;
            case "builder":
                return ChatColor.DARK_GREEN;
            case "sub++":
                return ChatColor.YELLOW;
            case "sub+":
                return ChatColor.GOLD;
            case "sub":
            case "hiddenmod":
                return ChatColor.GREEN;
            case "default":
                return ChatColor.GRAY;
        }
     */

    @AllArgsConstructor
    public enum PlayerRank {
        DEFAULT(0, "default", ChatColor.GRAY, ChatColor.GRAY + "Default"),
        SUB(1, "sub", ChatColor.GREEN, ChatColor.GREEN + "Subscriber"),
        SUB_PLUS(2, "sub+", ChatColor.GOLD, ChatColor.GOLD + "Subscriber+"),
        SUB_PLUS_PLUS(3, "sub++", ChatColor.YELLOW, ChatColor.YELLOW + "Subscriber++"),
        BUILDER(4, "builder", ChatColor.DARK_GREEN, ChatColor.DARK_GREEN + "Builder"),
        YOUTUBER(5, "youtube", ChatColor.RED, ChatColor.RED + "YouTuber"),
        SUPPORT(6, "support", ChatColor.BLUE, ChatColor.BLUE + "Support Agent"),
        PMOD(7, "pmod", ChatColor.WHITE, ChatColor.WHITE + "Player Moderator"),
        TRIALGM(8, "trialgm", ChatColor.AQUA, ChatColor.AQUA + "Trial Game Master"),
        GM(9, "gm", ChatColor.AQUA, ChatColor.AQUA + "Game Master"),
        HEADGM(10, "headgm", ChatColor.AQUA, ChatColor.AQUA + "Head Game Master"),
        DEV(11, "dev", ChatColor.AQUA, ChatColor.DARK_AQUA + "Developer");


        @Getter int rank;

        @Getter
        private String internalName;

        @Getter
        private ChatColor chatColor;

        @Getter
        private String prefix;


        public boolean isAtleast(PlayerRank rank){
            return getRank() >= rank.getRank();
        }
        public static PlayerRank getFromInternalName(String name) {
            return Arrays.stream(values()).filter(rank -> rank.getInternalName().equals(name.toLowerCase())).findFirst().orElse(null);
        }
    }
}