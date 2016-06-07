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
import net.dungeonrealms.game.player.chat.GameChat;
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

    volatile static HashMap<UUID, RankBlob> PLAYER_RANKS = new HashMap<>();
    private volatile static HashMap<String, RankBlob> RAW_RANKS = new HashMap<>();

    Block<Document> printDocumentBlock = document -> {
        Object info = document.get("rank");
        String name = ((Document) info).getString("name");
        long created = ((Document) info).getLong("created");
        List<String> permissions = (ArrayList<String>) ((Document) info).get("permissions");
        RAW_RANKS.put(name, new RankBlob(name, created, permissions));
        Utils.log.info("[RANK] [ASYNC] Grabbed Rank: " + ((Document) info).get("name") + " Storing its permissions!");
    };


    @Override
    public EnumPriority startPriority() {
        return EnumPriority.ARCHBISHOPS;
    }

    @Override
    public void startInitialization() {
        Database.ranks.find().forEach(printDocumentBlock);
        Utils.log.warning("[RANK] [ASYNC] Successfully grabbed all existing ranks!");
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
    public boolean createNewRank(String rankName) {
        if (RAW_RANKS.containsKey(rankName.toUpperCase())) return false;
        Document blankRankDocument =
                new Document("rank",
                        new Document("name", rankName)
                                .append("created", System.currentTimeMillis() / 1000L)
                                .append("permissions", new ArrayList<String>())
                );
        Database.ranks.insertOne(blankRankDocument);
        Utils.log.warning("Created a new Rank " + rankName);
        startInitialization();
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
        if (!RAW_RANKS.containsKey(sRank)) return;
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RANK, sRank, true);
        Player player = Bukkit.getPlayer(uuid);

        PermissionAttachment attachment = player.addAttachment(DungeonRealms.getInstance());
        RankBlob rank = RAW_RANKS.get(sRank.toUpperCase());
        player.sendMessage(ChatColor.GREEN + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "RANK" + ChatColor.GREEN + "] " + ChatColor.YELLOW + "Congratulations! Your rank is now: " + ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', GameChat.getPreMessage(player)));
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
        private List<String> permissions;

        public RankBlob(String name, long created, List<String> permissions) {
            this.name = name;
            this.created = created;
            this.permissions = permissions;
        }

        public String getName() {
            return name;
        }

        public long getCreated() {
            return created;
        }

        public List<String> getPermissions() {
            return permissions;
        }
    }

}
