package net.dungeonrealms.common.game.database.player.rank;

import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

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


//    public static boolean isRank(UUID uuid, Consumer<Rank> callback){
//        callback.accept(rank);
//    }
    public static boolean isRank(OfflinePlayer player, String name){
        return isRank(player, null, name);
    }

    /**
     * Less query intensive rank check
     *
     * @param player
     * @return boolean
     */
    public static boolean isRank(OfflinePlayer player, Document document, String name) {
        String rank;

        if(document == null)
            rank = Rank.getInstance().getRank(player.getUniqueId());
        else
            rank = Rank.getInstance().getRank(document);

        switch (name) {
            case "dev":
                return rank.equalsIgnoreCase("dev") && Arrays.asList(Constants.DEVELOPERS).contains(player.getName());

            case "gm":
                return rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("dev");

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

    public static boolean isDev(CommandSender commandSender) {
        return commandSender instanceof ConsoleCommandSender || (commandSender instanceof Player && Rank.isDev(((OfflinePlayer)commandSender)));
    }

    public static boolean isDev(Player player){//This is for legacy purposes.
        return isDev((OfflinePlayer)player);
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

    public static boolean isGMRank(String rank){
        return rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("headgm") || rank.equalsIgnoreCase("dev");
    }

    public static boolean isTrialGMRank(String rank){
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

    public static boolean isAtleastPMOD(String rank){
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

    public static String rankFromPrefix(String prefix) {
        switch (prefix.toLowerCase()) {
            case "dev":
                return ChatColor.DARK_AQUA + "Developer";
            case "headgm":
                return ChatColor.AQUA + "Head Game Master";
            case "gm":
                return ChatColor.AQUA + "Game Master";
            case "trialgm":
                return ChatColor.AQUA + "Trial Game Master";
            case "pmod":
                return ChatColor.WHITE + "Player Moderator";
            case "hiddenmod":
                return ChatColor.GREEN + "Hidden Player Moderator";
            case "support":
                return ChatColor.BLUE + "Support Agent";
            case "youtube":
                return ChatColor.RED + "YouTuber";
            case "builder":
                return ChatColor.DARK_GREEN + "Builder";
            case "sub++":
                return ChatColor.YELLOW + "Subscriber++";
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
     * Gets the players rank.
     *
     * @param doc
     * @return
     * @since 1.0
     */
    public String getRank(Document doc) {
        String rank = (String) DatabaseAPI.getInstance().getData(EnumData.RANK, doc);
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

        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RANK, sRank, true);
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