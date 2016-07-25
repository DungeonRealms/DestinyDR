package net.dungeonrealms.common.game.database;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.type.EnumData;
import net.dungeonrealms.common.game.database.type.EnumOperators;
import org.bson.Document;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by Nick on 8/29/2015.
 */

public class DatabaseAPI {

    private static DatabaseAPI instance = null;
    public volatile ConcurrentHashMap<UUID, Document> PLAYERS = new ConcurrentHashMap<>();

    private ExecutorService pool = Executors.newCachedThreadPool();


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
     * @param async
     * @param doAfterOptional an optional parameter allowing you to specify extra actions after the update query is
     *                        completed. doAfterOptional is executed async or sync based on the previous async parameter.
     * @since 1.0
     */
    public void update(UUID uuid, EnumOperators EO, EnumData variable, Object object, boolean async, Consumer<UpdateResult> doAfterOptional) {
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
        if (async) Futures.addCallback(update(uuid, EO, variable, object), new FutureCallback<UpdateResult>() {
            @Override
            public void onSuccess(@Nullable UpdateResult result) {
                if (Constants.debug)
                    Constants.log.info("[ASYNC] Successfully updated " + variable.name() + " data for " + uuid.toString());

                if (result.wasAcknowledged() && doAfterOptional != null)
                    pool.submit(() -> doAfterOptional.accept(result));
            }

            @ParametersAreNonnullByDefault
            @Override
            public void onFailure(Throwable throwable) {
                Constants.log.warning("[ASYNC] Failed to update player data for " + uuid.toString());
                throwable.printStackTrace();
            }
        });
        else {
            UpdateResult result = DatabaseDriver.collection.updateOne(Filters.eq("info.uuid", uuid.toString()), new Document(EO.getUO(), new Document(variable.getKey(), object)));
            if (doAfterOptional != null)
                doAfterOptional.accept(result);
        }
    }

    /**
     * {@link #update(UUID, EnumOperators, EnumData, Object, boolean, Consumer)}
     */
    public void update(UUID uuid, EnumOperators EO, EnumData variable, Object object, boolean async) {
        update(uuid, EO, variable, object, async, null);
    }


    private ListenableFuture<UpdateResult> update(UUID uuid, EnumOperators EO, EnumData variable, Object object) {
        return MoreExecutors.listeningDecorator(pool).submit(() -> DatabaseDriver.collection.updateOne(Filters.eq("info.uuid", uuid.toString()), new Document(EO.getUO(), new Document(variable.getKey(), object)))
        );
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
            Constants.log.warning("Retrieving " + uuid.toString() + "'s offline data...");
            doc = DatabaseDriver.collection.find(Filters.eq("info.uuid", uuid.toString())).first();
        }

        String[] key = data.getKey().split("\\.");
        Document rootDoc = (Document) doc.get(key[0]);
        if (rootDoc == null) return null;

        Object dataObj = rootDoc.get(key[1]);

        if (dataObj == null) return null;
        Class<?> clazz = dataObj.getClass();

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
        if (Constants.debug) {
            Constants.log.info("New playerdata requested for " + uuid + " from the database.");
            Constants.log.info(new Exception().getStackTrace()[1].getClassName() + " " + new Exception().getStackTrace()[1].getLineNumber());
        }
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
        Constants.log.warning("Retrieving " + playerName + "'s UUID from name..");
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
        Constants.log.warning("Retrieving " + uuid.toString() + "'s name..");
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
