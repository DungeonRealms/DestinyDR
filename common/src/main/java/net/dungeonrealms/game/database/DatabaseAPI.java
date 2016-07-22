package net.dungeonrealms.game.database;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.Constants;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nick on 8/29/2015.
 */

public class DatabaseAPI {

    private static DatabaseAPI instance = null;
    public volatile ConcurrentHashMap<UUID, Document> PLAYERS = new ConcurrentHashMap<>();


    public static DatabaseAPI getInstance() {
        if (instance == null) {
            instance = new DatabaseAPI();
        }
        return instance;
    }

    /**
     * Updates a players information in Mongo and returns the updated result.
     *
     * @param uuid
     * @param EO
     * @param variable
     * @param object
     * @param requestNew TRUE = WILL GET NEW DATA FROM MONGO.
     * @param async
     * @since 1.0
     */
    public void update(UUID uuid, EnumOperators EO, EnumData variable, Object object, boolean requestNew, boolean async) {
        if (async) {
            MongoUpdateThread.queries.add(Arrays.asList(Filters.eq("info.uuid", uuid.toString()), new Document(EO.getUO(), new Document(variable.getKey(), object)), new Document("requestNew", requestNew).append("uuid", uuid)));
        } else {
            DatabaseDriver.collection.updateOne(Filters.eq("info.uuid", uuid.toString()), new Document(EO.getUO(), new Document(variable.getKey(), object)));
        }
        if (requestNew && !async) {
            requestPlayer(uuid);
        }
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
        Document doc;

        if (PLAYERS.containsKey(uuid)) {
            // GRABBED CACHED DATA
            doc = PLAYERS.get(uuid);
        } else {
            Constants.log.warning("Retrieving player's offline data...");
            doc = DatabaseDriver.collection.find(Filters.eq("info.uuid", uuid.toString())).first();
        }

        String[] key = data.getKey().split("\\.");
        Document rootDoc = (Document)doc.get(key[0]);
        if (rootDoc == null) return null;

        Object dataObj = rootDoc.get(key[1]);
        Class<? extends Object> clazz = dataObj.getClass();

        Constants.log.info(clazz.toString());

        return rootDoc.get(key[1], clazz);
    }

    /**
     * Is fired to grab a player from Mongo
     * if they don't exist. Fire addNewPlayer() creation.
     *
     * @param uuid
     * @since 1.0
     */
    public boolean requestPlayer(UUID uuid) {
        Document doc = DatabaseDriver.collection.find(Filters.eq("info.uuid", uuid.toString())).first();
        if (doc == null) addNewPlayer(uuid);
        else PLAYERS.put(uuid, doc);

        return true;

        //GuildMechanics.getInstance().checkPlayerGuild(uuid);
        /*DatabaseDriver.collection.find(Filters.eq("info.uuid", uuid.toString())).first((document) -> {
            if (document != null) {
                Utils.log.info("Fetched information for uuid: " + uuid.toString());
                PLAYERS.put(uuid, document);
            } else {
                addNewPlayer(uuid);
            }
        });*/
    }

    public Object getValue(UUID uuid, EnumData data) {
        Document doc = DatabaseDriver.collection.find(Filters.eq("info.uuid", uuid.toString())).first();
        if (doc == null) return null;
        String[] key = data.getKey().split("\\.");
        return ((Document) doc.get(key[0])).get(key[1]);
    }

    public String getUUIDFromName(String playerName) {
        Document doc = DatabaseDriver.collection.find(Filters.eq("info.username", playerName.toLowerCase())).first();
        if (doc == null) return "";
        return ((Document) doc.get("info")).get("uuid", String.class);
    }

    public Document getDocumentFromAddress(String ipAddress) {
        return DatabaseDriver.collection.find(Filters.eq("info.ipAddress", ipAddress)).first();
    }

    public String getFormattedShardName(UUID uuid) {
        boolean isOnline = (boolean) getInstance().getData(EnumData.IS_PLAYING, uuid);
        if (!isOnline) {
            return "None";
        }
        Document doc = DatabaseDriver.collection.find(Filters.eq("info.uuid", uuid.toString())).first();
        if (doc == null)
            return "";
        String name = ((Document) doc.get("info")).get("current", String.class);
        return name.split("(?=[0-9])", 2)[0].toUpperCase() + "-" + name.split("(?=[0-9])", 2)[1];
    }

    public String getOfflineName(UUID uuid) {
        Document doc = DatabaseDriver.collection.find(Filters.eq("info.uuid", uuid.toString())).first();
        if (doc == null) {
            return "";
        } else {
            return ((Document) doc.get("info")).get("username", String.class);
        }
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
                                .append("username", "")
                                .append("health", 50)
                                .append("gems", 0)
                                .append("ecash", 0)
                                .append("isCombatLogged", false)
                                .append("ipAddress", "")
                                .append("firstLogin", System.currentTimeMillis() / 1000L)
                                .append("lastLogin", 0L)
                                .append("lastLogout", 0L)
                                .append("freeEcash", 0L)
                                .append("lastShardTransfer", 0L)
                                .append("netLevel", 1)
                                .append("experience", 0)
                                .append("hearthstone", "Cyrennica")
                                .append("currentLocation", "")
                                .append("isPlaying", true)
                                .append("friends", new ArrayList<>())
                                .append("alignment", "lawful")
                                .append("alignmentTime", 0)
                                .append("guild", "")
                                .append("shopOpen", false)
                                .append("foodLevel", 20)
                                .append("shopLevel", 1)
                                .append("muleLevel", 1)
                                .append("loggerdied", false)
                                .append("enteringrealm", "")
                                .append("activepet", "")
                                .append("activemount", "")
                                .append("activetrail", "")
                                .append("activemountskin", ""))
                        .append("attributes",
                                new Document("bufferPoints", 6)
                                        .append("strength", 0)
                                        .append("dexterity", 0)
                                        .append("intellect", 0)
                                        .append("vitality", 0)
                                        .append("resets", 0)
                                        .append("freeresets", 0))
                        .append("realm",
                                new Document("uploading", false)
                                        .append("title", "")
                                        .append("lastReset", 0L)
                                        .append("upgrading", false)
                                        .append("tier", 1))
                        .append("collectibles",
                                new Document("achievements", new ArrayList<String>())
                                        .append("mounts", new ArrayList<String>())
                                        .append("pets", new ArrayList<String>())
                                        .append("particles", new ArrayList<String>())
                                        .append("mountskins", new ArrayList<String>()))
                        .append("toggles",
                                new Document("debug", true)
                                        .append("trade", false)
                                        .append("tradeChat", true)
                                        .append("globalChat", false)
                                        .append("receiveMessage", true)
                                        .append("pvp", false)
                                        .append("duel", true)
                                        .append("chaoticPrevention", true)
                                        .append("tips", true))
                        .append("portalKeyShards",
                                new Document("tier1", 0)
                                        .append("tier2", 0)
                                        .append("tier3", 0)
                                        .append("tier4", 0)
                                        .append("tier5", 0))
                        .append("notices",
                                new Document("guildInvitation", null)
                                        .append("friendRequest", new ArrayList<String>())
                                        .append("mailbox", new ArrayList<String>()))
                        .append("rank",
                                new Document("expiration_date", 0)
                                        .append("rank", "DEFAULT"))
                        .append("punishments",
                                new Document("muted", 0L)
                                        .append("banned", 0L)
                                        .append("muteReason", "")
                                        .append("banReason", ""))
                        .append("inventory",
                                new Document("collection_bin", "")
                                        .append("mule", "empty")
                                        .append("storage", "")
                                        .append("level", 1)
                                        .append("player", "")
                                        .append("armor", new ArrayList<String>())
                                        .append("itemuids", new HashSet<String>()))
                        .append("stats",
                                new Document("player_kills", 0)
                                        .append("lawful_kills", 0)
                                        .append("unlawful_kills", 0)
                                        .append("deaths", 0)
                                        .append("monster_kills_t1", 0)
                                        .append("monster_kills_t2", 0)
                                        .append("monster_kills_t3", 0)
                                        .append("monster_kills_t4", 0)
                                        .append("monster_kills_t5", 0)
                                        .append("boss_kills_mayel", 0)
                                        .append("boss_kills_burick", 0)
                                        .append("boss_kills_infernalAbyss", 0)
                                        .append("loot_opened", 0)
                                        .append("duels_won", 0)
                                        .append("duels_lost", 0)
                                        .append("ore_mined", 0)
                                        .append("fish_caught", 0)
                                        .append("orbs_used", 0)
                                        .append("time_played", 0)
                                        .append("successful_enchants", 0)
                                        .append("failed_enchants", 0)
                                        .append("ecash_spent", 0)
                                        .append("gems_earned", 0)
                                        .append("gems_spent", 0));
        DatabaseDriver.collection.insertOne(newPlayerDocument);
        requestPlayer(uuid);
        Constants.log.info("Requesting new data for : " + uuid);
    }


}
