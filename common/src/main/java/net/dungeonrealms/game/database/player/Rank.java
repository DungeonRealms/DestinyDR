package net.dungeonrealms.game.database.player;

import net.dungeonrealms.Constants;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Nick on 9/27/2015.
 */
public class Rank {

    static Rank instance = null;
    volatile static HashMap<UUID, String> PLAYER_RANKS = new HashMap<>();

    public static Rank getInstance() {
        if (instance == null) {
            instance = new Rank();
        }
        return instance;
    }

    /**
     * Less query intensive rank check
     *
     * @param player
     * @return boolean
     */
    public static boolean isRank(OfflinePlayer player, String name) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());

        switch (name) {
            case "dev":
                return rank.equalsIgnoreCase("dev") && Arrays.asList(Constants.DEVELOPERS).contains(player.getName());

            case "gm":
                return rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("dev") || player.isOp();

            case "support":
                return rank.equalsIgnoreCase("support") || rank.equalsIgnoreCase("dev");

            case "pmod":
                return rank.equalsIgnoreCase("pmod") || rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("dev");

            case "youtube":
                return rank.equalsIgnoreCase("youtube") || rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("dev");

            case "subscriber":
            case "sub":
                return rank != null && !rank.equalsIgnoreCase("default");

            default:
                return rank != null && rank.equalsIgnoreCase("default");
        }
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
     * Returns true if the user has the rank "dev" or "gm". Opped players are also considered a GM.
     *
     * @param player
     * @return boolean
     */
    public static boolean isGM(OfflinePlayer player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("headgm") || rank.equalsIgnoreCase("dev") || player.isOp();
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
        return rank.equalsIgnoreCase("pmod") || rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("headgm") || rank.equalsIgnoreCase("dev");
    }

    /**
     * Returns true if the user has the rank "dev", "gm", "pmod" or "youtube".
     *
     * @param player
     * @return boolean
     */
    public static boolean isYouTuber(OfflinePlayer player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank.equalsIgnoreCase("youtube") || rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("headgm") || rank.equalsIgnoreCase("dev");
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

    public static String rankFromPrefix(String prefix) {
        switch (prefix.toLowerCase()) {
            case "dev":
                return ChatColor.DARK_AQUA + "Developer";
            case "headgm":
                return ChatColor.AQUA + "Head Game Master";
            case "gm":
                return ChatColor.AQUA + "Game Master";
            case "pmod":
                return ChatColor.WHITE + "Player Moderator";
            case "support":
                return ChatColor.BLUE + "Support Agent";
            case "youtube":
                return ChatColor.RED + "YouTuber";
            case "builder":
                return ChatColor.DARK_AQUA + "Builder";
            case "sub++":
                return ChatColor.DARK_AQUA + "Subscriber++";
            case "sub+":
                return ChatColor.GOLD + "Subscriber+";
            case "sub":
                return ChatColor.GREEN + "Subscriber";
            case "default":
                return ChatColor.GRAY + "Default";
        }

        // Could not find rank.
        return null;
    }

    public static ChatColor colorFromRank(String prefix) {
        switch (prefix.toLowerCase()) {
            case "dev":
                return ChatColor.AQUA;
            case "headgm":
            case "gm":
                return ChatColor.AQUA;
            case "pmod":
                return ChatColor.WHITE;
            case "support":
                return ChatColor.BLUE;
            case "youtube":
                return ChatColor.RED;
            case "builder":
                return ChatColor.DARK_AQUA;
            case "sub++":
                return ChatColor.DARK_AQUA;
            case "sub+":
                return ChatColor.GOLD;
            case "sub":
                return ChatColor.GREEN;
            case "default":
                return ChatColor.GRAY;
        }

        // Could not find rank.
        return ChatColor.GRAY;
    }

    /**
     * Gets the players rank.
     *
     * @param uuid
     * @return
     * @since 1.0
     */
    public String getRank(UUID uuid) {
        String rank = (String) DatabaseAPI.getInstance().getData(EnumData.RANK, uuid);
        return (rank == null || rank.equals("") ? "default" : rank).toUpperCase();
    }

    /**
     * Sets a players rank.
     *
     * @param uuid
     * @param sRank
     * @since 1.0
     */
    public void setRank(UUID uuid, String sRank) {
        String newRank = Rank.rankFromPrefix(sRank);

        if (newRank == null) return; // @todo: Remove RAW_RANKS, replace with the fixed list.

        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RANK, sRank, true, true);
        Player player = Bukkit.getPlayer(uuid);

        player.sendMessage("                 " + ChatColor.YELLOW + "Your rank is now: " + newRank);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
    }

    /**
     * Listens in the DatabaseDriver class when the players
     * data is first returned to assign the proper
     * rank to the player!
     *
     * @param uuid
     */
    public void doGet(UUID uuid) {
        PLAYER_RANKS.put(uuid, (String) DatabaseAPI.getInstance().getData(EnumData.RANK, uuid));
    }

}
