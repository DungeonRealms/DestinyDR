package net.dungeonrealms.common.game.database;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.concurrent.MongoAccessThread;
import net.dungeonrealms.common.game.database.concurrent.query.BulkWriteQuery;
import net.dungeonrealms.common.game.database.concurrent.query.DocumentSearchQuery;
import net.dungeonrealms.common.game.database.concurrent.query.SingleUpdateQuery;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.network.ShardInfo;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by Nick on 8/29/2015.
 */

@SuppressWarnings("unchecked")
public class DatabaseAPI {

    private static DatabaseAPI instance = null;

    public volatile Map<UUID, Document> PLAYERS = new ConcurrentHashMap<>();

    private volatile Map<String, String> CACHED_UUIDS = new ConcurrentHashMap<>();

    private final ExecutorService SERVER_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("MONGODB Server Collection Thread").build());

    public static DatabaseAPI getInstance() {
        if (instance == null) {
            instance = new DatabaseAPI();
        }
        return instance;
    }

    public void startInitialization(String shard) {
        Document doc = DatabaseInstance.shardData.find(Filters.eq("shard", shard)).first();
        if (doc == null) {
            createNewShardCollection(shard);
        }
    }

    /**
     * @param operations      Bulk write operations
     * @param async           Run Async?
     * @param doAfterOptional an optional parameter allowing you to specify extra actions after the update query is
     *                        completed. doAfterOptional is executed async or sync based on the previous async parameter.
     */
    public void bulkUpdate(List<UpdateOneModel<Document>> operations, boolean async, Consumer<BulkWriteResult> doAfterOptional) {
        if (async) {
            MongoAccessThread.submitQuery(new BulkWriteQuery<>(DatabaseInstance.playerData, operations, doAfterOptional));
        } else {
            BulkWriteResult result = DatabaseInstance.playerData.bulkWrite(operations);
            if (Constants.debug)
                Constants.log.info("[Database] ASYNC Executed bulk write operation. Modifications: " + result.getModifiedCount());

            if (doAfterOptional != null)
                doAfterOptional.accept(result);
        }
    }

    /**
     * Updates a players information in Mongo and returns the updated result.
     *
     * @param uuid
     * @param EO
     * @param variable
     * @param object
     * @param async
     * @param doAfterOptional an optional parameter allowing you to specify extra actions after the update query is
     *                        completed. doAfterOptional is executed async or sync based on the previous async parameter.
     * @since 1.0
     */
    public void update(UUID uuid, EnumOperators EO, EnumData variable, Object object, boolean async, boolean updateDatabase, Consumer<UpdateResult> doAfterOptional) {
        if (PLAYERS.containsKey(uuid)) { // update local data
            Document localDoc = PLAYERS.get(uuid);
            String[] key = variable.getKey().split("\\.");
            Document rootDoc = (Document) localDoc.get(key[0]);
            Object data = rootDoc.get(key[1]);
            switch (EO) {
                case $SET:
                    rootDoc.put(key[1], object);
                    break;
                case $INC:
                    if (data instanceof Integer)
                        rootDoc.put(key[1], ((Integer) object) + ((Integer) data));
                    else if (data instanceof Double)
                        rootDoc.put(key[1], ((Double) object) + ((Double) data));
                    else if (data instanceof Float)
                        rootDoc.put(key[1], ((Float) object) + ((Float) data));
                    else if (data instanceof Long)
                        rootDoc.put(key[1], ((Long) object) + ((Long) data));
                    break;
                case $MUL:
                    if (data instanceof Integer)
                        rootDoc.put(key[1], ((Integer) object) * ((Integer) data));
                    else if (data instanceof Double)
                        rootDoc.put(key[1], ((Double) object) * ((Double) data));
                    else if (data instanceof Float)
                        rootDoc.put(key[1], ((Float) object) * ((Float) data));
                    else if (data instanceof Long)
                        rootDoc.put(key[1], ((Long) object) * ((Long) data));
                    break;
                case $PUSH:
                    ((ArrayList) data).add(object);
                    break;
                case $PULL:
                    ((ArrayList) data).remove(object);
                    break;
                default:
                    break;
            }
        }

        if (updateDatabase)
            if (async)
                MongoAccessThread.submitQuery(new SingleUpdateQuery<>(DatabaseInstance.playerData, Filters.eq("info.uuid", uuid.toString()), new Document(EO.getUO(), new Document(variable.getKey(), object)), doAfterOptional));
            else {
                UpdateResult result = DatabaseInstance.playerData.updateOne(Filters.eq("info.uuid", uuid.toString()), new Document(EO.getUO(), new Document(variable.getKey(), object)), MongoAccessThread.uo);
                if (doAfterOptional != null)
                    doAfterOptional.accept(result);


                if (Bukkit.isPrimaryThread()) {
                    Constants.log.warning("[Database] Updating " + uuid.toString() + "'s player data on the main thread");

                    if (Constants.debug)
                        printTrace();
                }
            }
    }

    /**
     * {@link #update(UUID, EnumOperators, EnumData, Object, boolean, boolean, Consumer)}
     */
    public void update(UUID uuid, EnumOperators EO, EnumData variable, Object object, boolean async) {
        update(uuid, EO, variable, object, async, true, null);
    }

    /**
     * {@link #update(UUID, EnumOperators, EnumData, Object, boolean, boolean, Consumer)}
     */
    public void update(UUID uuid, EnumOperators EO, EnumData variable, Object object, boolean async, Consumer<UpdateResult> doAfterOptional) {
        update(uuid, EO, variable, object, async, true, doAfterOptional);
    }

    /**
     * {@link #update(UUID, EnumOperators, EnumData, Object, boolean, boolean, Consumer)}
     */
    public void update(UUID uuid, EnumOperators EO, EnumData variable, Object object, boolean async, boolean updateDatabase) {
        update(uuid, EO, variable, object, async, updateDatabase, null);
    }

    public void updateShardCollection(String shard, EnumOperators EO, String variable, Object value, boolean async, Consumer<UpdateResult> doAfterOptional) {
        UpdateOptions uo = new UpdateOptions();
        uo.upsert(true);

        if (async)
            SERVER_EXECUTOR_SERVICE.submit(() -> DatabaseInstance.shardData.updateOne(Filters.eq("shard", shard), new
                    Document(EO.getUO(), new Document(variable, value))), uo);
        else {
            UpdateResult result = DatabaseInstance.shardData.updateOne(Filters.eq("shard", shard), new Document(EO.getUO(), new Document(variable, value)), uo);
            if (doAfterOptional != null)
                doAfterOptional.accept(result);

            if (Bukkit.isPrimaryThread()) {
                Constants.log.warning("[Database] Updating shard_data collection on the main thread.");
                if (Constants.debug)
                    printTrace();
            }
        }
    }

    public void updateShardCollection(String shard, EnumOperators EO, String variable, Object value, boolean async) {
        updateShardCollection(shard, EO, variable, value, async, null);
    }


    /**
     * Safely Returns the object that's requested
     * based on UUID.
     *
     * @param data Data type
     * @param uuid UUID
     * @return Requested data
     */
    public Object getData(EnumData data, UUID uuid) {
        Document doc;

        // GRABBED CACHED DOCUMENT
        if (PLAYERS.containsKey(uuid)) doc = PLAYERS.get(uuid);
        else {
            long currentTime = 0;
            // we should never be getting offline data sync.

            if (Bukkit.isPrimaryThread()) {
                Constants.log.info("[Database] Requested for " + uuid + "'s document from the database on the main thread.");

                if (Constants.debug)
                    printTrace();
            }

            if (Constants.debug) currentTime = System.currentTimeMillis();

            doc = DatabaseInstance.playerData.find(Filters.eq("info.uuid", uuid.toString())).first();

            if (Constants.debug) {
                Constants.log.info("[Database] Player document retrieved in " + String.valueOf(System.currentTimeMillis() - currentTime) + " ms.");
                printTrace();
            }
        }

        return getData(data, doc);
    }

    /**
     * Safely Returns the object that's requested
     * based on UUID.
     *
     * @param data     Data type
     * @param document User's document
     * @return Requested data
     */
    public Object getData(EnumData data, Document document) {
        String[] key = data.getKey().split("\\.");
        Document rootDoc = (Document) document.get(key[0]);
        if (rootDoc == null) return null;

        Object dataObj = rootDoc.get(key[1]);

        if (dataObj == null) return null;
        Class<?> clazz = dataObj.getClass();

        return rootDoc.get(key[1], clazz);
    }


    /**
     * Retrieve's user's document asynchronously
     *
     * @param ipAddress User's IP address
     * @param doAfter   Executed after document is retrieved
     */
    public void retrieveDocumentFromAddress(String ipAddress, Consumer<Document> doAfter) {
        MongoAccessThread.submitQuery(new DocumentSearchQuery<>(DatabaseInstance.playerData, Filters.eq("info.ipAddress", ipAddress), doAfter));
    }

    /**
     * Retrieve's user's document asynchronously
     *
     * @param uuid    User's UUID
     * @param doAfter Executed after document is retrieved
     */
    public void retrieveDocumentFromUUID(UUID uuid, Consumer<Document> doAfter) {
        MongoAccessThread.submitQuery(new DocumentSearchQuery<>(DatabaseInstance.playerData, Filters.eq("info.uuid", uuid.toString()), doAfter));
    }

    /**
     * Retrieve's user's document asynchronously
     *
     * @param username User's username
     * @param doAfter  Executed after document is retrieved
     */
    public void retrieveDocumentFromUsername(String username, Consumer<Document> doAfter) {
        MongoAccessThread.submitQuery(new DocumentSearchQuery<>(DatabaseInstance.playerData, Filters.eq("info.username", username.toLowerCase()), doAfter));
    }


    /**
     * Is fired to grab a player from Mongo
     * if they don't exist. Fire addNewPlayer() creation.
     *
     * @param uuid
     * @since 1.0
     */
    public void requestPlayer(UUID uuid, boolean async) {
        requestPlayer(uuid, async, null);
    }

    /**
     * Is fired to grab a player from Mongo
     * if they don't exist. Fire addNewPlayer() creation.
     *
     * @param uuid
     * @since 1.0
     */
    public void requestPlayer(UUID uuid, boolean async, Runnable doAfterOptional) {
        if (async) {
            retrieveDocumentFromUUID(uuid, doc -> {
                if (doc == null) {
                    addNewPlayer(uuid, async);
                } else {
                    PLAYERS.put(uuid, doc);
                }


                if (Constants.debug) {
                    Constants.log.info("[Database] [ASYNC] Requested for " + uuid + "'s document from the database.");
                    printTrace();
                }

                if (doAfterOptional != null)
                    doAfterOptional.run();
            });
        } else {
            Document doc = DatabaseInstance.playerData.find(Filters.eq("info.uuid", uuid.toString())).first();
            if (doc == null) addNewPlayer(uuid, async);
            else PLAYERS.put(uuid, doc);

            if (Constants.debug) {
                Constants.log.info("[Database] Requested for " + uuid + "'s document from the database.");
                printTrace();
            }

            if (Bukkit.isPrimaryThread()) {
                Constants.log.info("[Database] Requested for " + uuid + "'s document from the database on the main thread.");
                if (Constants.debug)
                    printTrace();
            }

            if (doAfterOptional != null)
                doAfterOptional.run();
        }
    }

    public String getUUIDFromName(String playerName) {
        if (CACHED_UUIDS.containsKey(playerName)) return CACHED_UUIDS.get(playerName);

        if (Constants.debug) {
            Constants.log.info("[Database] Retrieving for " + playerName + " 's UUID from name.");
            printTrace();
        }


        if (Bukkit.isPrimaryThread()) {
            Constants.log.warning("[Database] Retrieving " + playerName + " 's UUID from name on the main thread.");

            if (Constants.debug)
                printTrace();
        }

        Document doc = DatabaseInstance.playerData.find(Filters.eq("info.username", playerName.toLowerCase())).first();
        if (doc == null) return "";
        String uuidString = ((Document) doc.get("info")).get("uuid", String.class);
        CACHED_UUIDS.put(playerName, uuidString);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                CACHED_UUIDS.remove(playerName);
            }
        }, 500);
        return uuidString;
    }

    public String getFormattedShardName(UUID uuid) {
        boolean isOnline = (boolean) getInstance().getData(EnumData.IS_PLAYING, uuid);
        if (!isOnline) return "None";
        ShardInfo shard = ShardInfo.getByPseudoName((String) getData(EnumData.CURRENTSERVER, uuid));
        return shard != null ? shard.getShardID() : "";
    }

    public String getOfflineName(UUID uuid) {
        if (Bukkit.isPrimaryThread()) {
            Constants.log.info("[Database] Retrieving for " + uuid.toString() + "'s name on the main thread.");
            if (Constants.debug)
                printTrace();
        }

        Document doc = DatabaseInstance.playerData.find(Filters.eq("info.uuid", uuid.toString())).first();
        if (doc == null) return "";
        else {
            return ((Document) doc.get("info")).get("username", String.class);
        }
    }

    private void printTrace() {
        StackTraceElement trace = new Exception().getStackTrace()[2];

        Constants.log.info("[Database] Class: " + trace.getClassName());
        Constants.log.info("[Database] Method: " + trace.getMethodName());
        Constants.log.info("[Database] Line: " + trace.getLineNumber());
    }

    public Object getShardData(String shard, String data) {
        Document doc = DatabaseInstance.shardData.find(Filters.eq("shard", shard)).first();

        if (doc == null) return null;

        String[] key = data.split("\\.");
        if (!(doc.get(key[0]) instanceof Document)) return null;
        Document rootDoc = (Document) doc.get(key[0]);
        if (rootDoc == null) return null;

        Object dataObj = rootDoc.get(key[1]);

        if (dataObj == null) return null;
        Class<?> clazz = dataObj.getClass();

        return rootDoc.get(key[1], clazz);
    }

    public void createNewShardCollection(String shard) {
        DatabaseInstance.shardData.insertOne(new Document("shard", shard));
        Constants.log.info("[MONGO] Created new document in the shard_data collection for shard " + shard);
    }

    /**
     * Adds a new player to Mongo Creates Document here.
     *
     * @param uuid
     * @since 1.0
     */

    private void addNewPlayer(UUID uuid, boolean async) {
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
                                        .append("buffs", new ArrayList<String>())
                                        .append("particles", new ArrayList<String>())
                                        .append("mountskins", new ArrayList<String>()))
                        .append("toggles",
                                new Document("debug", true)
                                        .append("trade", false)
                                        .append("tradeChat", true)
                                        .append("globalChat", false)
                                        .append("receiveMessage", true)
                                        .append("soundtrack", true)
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
                                        .append("mailbox", new ArrayList<String>())
                                        .append("lastBuild", "")
                                        .append("lastVote", 0L))
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

        DatabaseInstance.playerData.insertOne(newPlayerDocument);
        requestPlayer(uuid, async);
        Constants.log.info("[Database] Requesting new data for : " + uuid);
    }


    public void stopInvocation() {
        SERVER_EXECUTOR_SERVICE.shutdown();
    }
}
