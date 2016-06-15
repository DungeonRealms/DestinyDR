package net.dungeonrealms.game.player.rank;

import com.mongodb.Block;
import com.mongodb.client.model.Filters;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.Database;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by Nick on 9/27/2015.
 */
public class Rank implements GenericMechanic {

    static Rank instance = null;

    public static Rank getInstance() {
        if (instance == null) {
            instance = new Rank();
        }
        return instance;
    }

    volatile static HashMap<UUID, String> PLAYER_RANKS = new HashMap<>();


    @Override
    public EnumPriority startPriority() {
        return EnumPriority.ARCHBISHOPS;
    }

    @Override
    public void startInitialization() {
        Utils.log.warning("[RANK] Init finished!");
    }

    @Override
    public void stopInvocation() {

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
        return (rank == null || rank == "" ? "default" : rank).toUpperCase();
    }

    /**
     * Adds a <String>Permission</String> to a Rank's ArrayList</>
     *
     * @param rank
     * @param permission
     * @since 1.0
     */
    public void addPermission(String rank, String permission) {
        Database.ranks.updateOne(Filters.eq("rank.name", rank.toUpperCase()), new Document(EnumOperators.$PUSH.getUO(), new Document("rank.permissions", permission)));
        Utils.log.info("[ASYNC] DatabaseAPI update() called .. addPermission()... METHOD");
        startInitialization();
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

        PermissionAttachment attachment = player.addAttachment(DungeonRealms.getInstance());
        player.sendMessage("                 " + ChatColor.YELLOW + "Your rank is now: " + newRank);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 63f);
    }

    /**
     * Listens in the Database class when the players
     * data is first returned to assign the proper
     * rank to the player!
     *
     * @param uuid
     */
    public void doGet(UUID uuid) {
        PLAYER_RANKS.put(uuid, (String) DatabaseAPI.getInstance().getData(EnumData.RANK, uuid));
    }

    /**
     * Returns true if user has the rank "dev".
     * @todo: Remove "DEV" rank, use "GM" rank and check if in getDevelopers array in the DungeonRealms class.
     *
     * @param player
     * @return boolean
     */
    public static boolean isDev(Player player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank.equalsIgnoreCase("dev") && DungeonRealms.getInstance().getDevelopers().contains(player.getDisplayName());
    }

    /**
     * Returns true if the user has the rank "dev" or "gm". Opped players are also considered a GM.
     *
     * @param player
     * @return boolean
     */
    public static boolean isGM(Player player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("dev") || player.isOp();
    }

    /**
     * Returns true if the user has the rank "dev" or "support".
     *
     * @param player
     * @return boolean
     */
    public static boolean isSupport(Player player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank.equalsIgnoreCase("support") || rank.equalsIgnoreCase("dev");
    }

    /**
     * Returns true if the user has the rank "dev", "gm" or "pmod".
     *
     * @param player
     * @return boolean
     */
    public static boolean isPMOD(Player player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank.equalsIgnoreCase("pmod") || rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("dev");
    }

    /**
     * Returns true if the user has the rank "dev", "gm", "pmod" or "youtube".
     *
     * @param player
     * @return boolean
     */
    public static boolean isYouTuber(Player player) {
        String rank = Rank.getInstance().getRank(player.getUniqueId());
        return rank.equalsIgnoreCase("youtube") || rank.equalsIgnoreCase("pmod") || rank.equalsIgnoreCase("gm") || rank.equalsIgnoreCase("dev");
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

}
