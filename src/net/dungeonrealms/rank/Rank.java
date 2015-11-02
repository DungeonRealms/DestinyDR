package net.dungeonrealms.rank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import com.mongodb.Block;
import com.mongodb.client.model.Filters;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.mongo.Database;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;

/**
 * Created by Nick on 9/27/2015.
 */
public class Rank implements GenericMechanic{

    static Rank instance = null;

    public static Rank getInstance() {
        if (instance == null) {
            instance = new Rank();
        }
        return instance;
    }

    volatile static HashMap<UUID, RankBlob> PLAYER_RANKS = new HashMap<>();
    private volatile static HashMap<String, RankBlob> RAW_RANKS = new HashMap<>();

    Block<Document> printDocumentBlock = document -> {
        Object info = document.get("rank");
        String name = ((Document) info).getString("name");
        long created = ((Document) info).getLong("created");
        String prefix = ((Document) info).getString("prefix");
        String suffix = ((Document) info).getString("suffix");
        List<String> permissions = (ArrayList<String>) ((Document) info).get("permissions");
        RAW_RANKS.put(name, new RankBlob(name, created, prefix, suffix, permissions));
        Utils.log.info("[RANK] [ASYNC] Grabbed Rank: " + ((Document) info).get("name") + " Storing its permissions!");
    };


    @Override
    public EnumPriority startPriority() {
        return EnumPriority.ARCHBISHOPS;
    }

    public void startInitialization() {
        Database.ranks.find().forEach(printDocumentBlock, (aVoid, throwable) -> Utils.log.warning("[RANK] [ASYNC] Successfully grabbed all existing ranks!"));
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
    public RankBlob getRank(UUID uuid) {
        String rank = (String) DatabaseAPI.getInstance().getData(EnumData.RANK, uuid);
        return RAW_RANKS.get(rank);
    }

    /**
     * Creates a new rank on Mongo Collection ("rank")
     *
     * @param rankName
     * @since 1.0
     */
    public boolean createNewRank(String rankName, String prefix, String suffix) {
        if (RAW_RANKS.containsKey(rankName.toUpperCase())) return false;
        Document blankRankDocument =
                new Document("rank",
                        new Document("name", rankName)
                                .append("created", System.currentTimeMillis() / 1000L)
                                .append("prefix", prefix)
                                .append("suffix", suffix)
                                .append("permissions", new ArrayList<String>())
                );
        Database.ranks.insertOne(blankRankDocument, (aVoid, throwable) -> {
            Utils.log.warning("Created a new Rank " + rankName);
            startInitialization();
        });
        return true;
    }

    /**
     * Adds a <String>Permission</String> to a Rank's ArrayList</>
     *
     * @param rank
     * @param permission
     * @since 1.0
     */
    public void addPermission(String rank, String permission) {
        Database.ranks.updateOne(Filters.eq("rank.name", rank.toUpperCase()), new Document(EnumOperators.$PUSH.getUO(), new Document("rank.permissions", permission)),
                (result, t) -> {
                    Utils.log.info("[ASYNC] DatabaseAPI update() called .. addPermission()... METHOD");
                    startInitialization();
                });
    }

    /**
     * Sets a players rank.
     *
     * @param uuid
     * @param sRank
     * @since 1.0
     */
    public void setRank(UUID uuid, String sRank) {
        if (!RAW_RANKS.containsKey(sRank)) return;
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RANK, sRank, true);
        Player player = Bukkit.getPlayer(uuid);

        PermissionAttachment attachment = player.addAttachment(DungeonRealms.getInstance());
        RankBlob rank = RAW_RANKS.get(sRank.toUpperCase());
        player.sendMessage(ChatColor.GREEN + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "RANK" + ChatColor.GREEN + "] " + ChatColor.YELLOW + "Congratulations! Your rank is now: " + ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', rank.getPrefix()));
        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 63f);
        for (String s : rank.getPermissions()) {
            attachment.setPermission(s, true);
        }
    }

    /**
     * Listens in the Database class when the players
     * data is first returned to assign the proper
     * rank to the player!
     *
     * @param uuid
     */
    public void doGet(UUID uuid) {
        PLAYER_RANKS.put(uuid, RAW_RANKS.get(DatabaseAPI.getInstance().getData(EnumData.RANK, uuid)));
    }

    public class RankBlob {
        private String name;
        private long created;
        private String prefix;
        private String suffix;
        private List<String> permissions;

        public RankBlob(String name, long created, String prefix, String suffix, List<String> permissions) {
            this.name = name;
            this.created = created;
            this.prefix = prefix;
            this.suffix = suffix;
            this.permissions = permissions;
        }

        public String getName() {
            return name;
        }

        public long getCreated() {
            return created;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }

        public List<String> getPermissions() {
            return permissions;
        }
    }

}
