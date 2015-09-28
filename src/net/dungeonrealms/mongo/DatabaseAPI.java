package net.dungeonrealms.mongo;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.rank.Subscription;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Nick on 8/29/2015.
 */
public class DatabaseAPI {

    private static DatabaseAPI instance = null;

    public static DatabaseAPI getInstance() {
        if (instance == null) {
            instance = new DatabaseAPI();
        }
        return instance;
    }

    public static volatile HashMap<UUID, Document> PLAYERS = new HashMap<>();
    private static volatile ArrayList<UUID> REQUEST_NEW_DATA = new ArrayList<>();

    /**
     * Updates a players information in Mongo and returns the updated result.
     *
     * @param uuid
     * @param EO
     * @param variable
     * @param object
     * @since 1.0
     */
    public void update(UUID uuid, EnumOperators EO, String variable, Object object) {
        Database.collection.updateOne(Filters.eq("info.uuid", uuid.toString()), new Document(EO.getUO(), new Document(variable, object)),
                (result, t) -> {
                    Utils.log.info("DatabaseAPI update() called ...");
                    if (t == null) {
                        REQUEST_NEW_DATA.add(uuid);
                    }
                });
    }

    /**
     * Returns the object that's requested.
     *
     * @param data
     * @param uuid
     * @return
     * @since 1.0
     */
    public Object getData(EnumData data, UUID uuid) {
        switch (data) {
            /*
            Player Variables Main Document().
             */
            case HEALTH:
                return ((Document) PLAYERS.get(uuid).get("info")).get("health", Integer.class);
            case FIRST_LOGIN:
                return ((Document) PLAYERS.get(uuid).get("info")).get("firstLogin", Long.class);
            case LAST_LOGIN:
                return ((Document) PLAYERS.get(uuid).get("info")).get("lastLogin", Long.class);
            case IS_PLAYING:
                return ((Document) PLAYERS.get(uuid).get("info")).get("isPlaying", Boolean.class);
            case LEVEL:
                return ((Document) PLAYERS.get(uuid).get("info")).get("netLevel", Integer.class);
            case GEMS:
                return ((Document) PLAYERS.get(uuid).get("info")).get("gems", Integer.class);
            case HEARTHSTONE:
                return ((Document) PLAYERS.get(uuid).get("info")).get("hearthstone", Location.class);
            case ECASH:
                return ((Document) PLAYERS.get(uuid).get("info")).get("ecash", Integer.class);
            case FRIENDS:
                return ((Document) PLAYERS.get(uuid).get("info")).get("friends", ArrayList.class);
            /*
            Rank Things. Different Sub-Document().
             */
            case RANK:
                return ((Document) PLAYERS.get(uuid).get("rank")).get("rank", String.class);
            case RANK_EXISTENCE:
                return ((Document) PLAYERS.get(uuid).get("rank")).get("lastPurchase", Long.class);
            case PURCHASE_HISTORY:
                return ((Document) PLAYERS.get(uuid).get("rank")).get("purchaseHistory", ArrayList.class);
            /*
            Player Attribute Variables
             */
            case STRENGTH:
                return ((Document) PLAYERS.get(uuid).get("info")).get("attributes.strength", Integer.class);
            case DEXTERITY:
                return ((Document) PLAYERS.get(uuid).get("info")).get("attributes.dexterity", Integer.class);
            case INTELLECT:
                return ((Document) PLAYERS.get(uuid).get("info")).get("attributes.intellect", Integer.class);
            case VITALITY:
                return ((Document) PLAYERS.get(uuid).get("info")).get("attributes.vitality", Integer.class);
            /*
            Player Storage
             */
            case INVENTORY_COLLECTION_BIN:
                return ((Document) PLAYERS.get(uuid).get("inventory")).get("collection_bin", String.class);
            case INVENTORY_MULE:
                return ((Document) PLAYERS.get(uuid).get("inventory")).get("mule", String.class);
            case INVENTORY_STORAGE:
                return ((Document) PLAYERS.get(uuid).get("inventory")).get("storage", String.class);
            case INVENTORY:
                return ((Document) PLAYERS.get(uuid).get("inventory")).get("player", String.class);
            default:
        }
        return null;
    }

    /**
     * Starts the Initialization of DatabaseAPI.
     *
     * @since 1.0
     */
    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> REQUEST_NEW_DATA.forEach(this::requestPlayer), 0, 1l);
    }

    /**
     * Is fired to grab a player from Mongo
     * if they don't exist. Fire addNewPlayer() creation.
     *
     * @param uuid
     * @since 1.0
     */
    public void requestPlayer(UUID uuid) {
        Database.collection.find(Filters.eq("info.uuid", uuid.toString())).first((document, throwable) -> {
            if (document == null) {
                addNewPlayer(uuid);
            } else if (document != null) {
                if (REQUEST_NEW_DATA.contains(uuid)) {
                    REQUEST_NEW_DATA.remove(uuid);
                }
                PLAYERS.put(uuid, document);
                //Put The player in the Rank List so it be be iterated and checked for subscription length.
                Subscription.PLAYER_RANK.add(uuid);
            }
        });
    }

    /**
     * Adds a new player to Mongo Creates Document here.
     *
     * @param uuid
     * @since 1.0
     */
    private void addNewPlayer(UUID uuid) {
        Document newPlayerDocument =
                new Document("info",
                        new Document("uuid", uuid.toString())
                                .append("health", 50)
                                .append("gems", 100)
                                .append("ecash", 0)
                                .append("firstLogin", System.currentTimeMillis() / 1000L)
                                .append("lastLogin", 0l)
                                .append("netLevel", 1)
                                .append("experience", 0f)
                                .append("hearthstone", "starter")
                                .append("isPlaying", true)
                                .append("friends", new ArrayList<>())
                                .append("attributes",
                                        new Document("strength", 1).append("dexterity", 1).append("intellect", 1).append("vitality", 1))
                                .append("collectibles",
                                        new Document("achievements", new ArrayList<String>())))
                        .append("rank",
                                new Document("lastPurchase", 0l)
                                        .append("purchaseHistory", new ArrayList<String>())
                                        .append("rank", "DEFAULT"))
                        .append("inventory",
                                new Document("collection_bin", "")
                                        .append("mule", "")
                                        .append("storage", "")
                                        .append("player", "")
                        );
        Database.collection.insertOne(newPlayerDocument, (aVoid, throwable) -> {
            REQUEST_NEW_DATA.add(uuid);
            Utils.log.info("Requesting new data for : " + uuid);
        });
    }

}
