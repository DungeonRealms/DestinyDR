package net.dungeonrealms.game.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import net.dungeonrealms.Callback;
import net.dungeonrealms.game.mastery.Utils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    public volatile ConcurrentHashMap<UUID, Document> PLAYERS = new ConcurrentHashMap<>();
    public volatile ConcurrentHashMap<String, Document> GUILDS = new ConcurrentHashMap<>();

    /**
     * Updates a players information in Mongo and returns the updated result.
     *
     * @param uuid
     * @param EO
     * @param variable
     * @param object
     * @param requestNew TRUE = WILL GET NEW DATA FROM MONGO.
     * @since 1.0
     */
    public void update(UUID uuid, EnumOperators EO, EnumData variable, Object object, boolean requestNew) {
        Database.collection.updateOne(Filters.eq("info.uuid", uuid.toString()), new Document(EO.getUO(), new Document(variable.getKey(), object)),
                (result, exception) -> {
                    Utils.log.info("DatabaseAPI update() called ...");
                    if (exception == null && requestNew) {
                        requestPlayer(uuid);
                    }
                });
    }

    /**
     * Updates a players information in Mongo and returns the updated result.
     *
     * @param uuid
     * @param EO
     * @param variable
     * @param object
     * @param requestNew TRUE = WILL GET NEW DATA FROM MONGO.
     * @param callback   Calls back the result.
     * @since 1.0
     */
    public void update(UUID uuid, EnumOperators EO, EnumData variable, Object object, boolean requestNew, Callback<UpdateResult> callback) {
        Database.collection.updateOne(Filters.eq("info.uuid", uuid.toString()), new Document(EO.getUO(), new Document(variable.getKey(), object)),
                (result, exception) -> {
                    callback.callback(exception, result);
                    Utils.log.info("DatabaseAPI update() called ...");
                    if (exception == null && requestNew) {
                        requestPlayer(uuid);
                    }
                });
    }

    /**
     * Updates specified Guild
     *
     * @param guildName  Name of the Guild.
     * @param EO         Type of Operation
     * @param variable   Variable to change i.e (info.icon)
     * @param object     New Data
     * @param requestNew Pull new guild once injected?
     * @since 1.0
     */
    public void updateGuild(String guildName, EnumOperators EO, EnumGuildData variable, Object object, boolean requestNew) {
        Database.guilds.updateOne(Filters.eq("info.name", guildName.toUpperCase()), new Document(EO.getUO(), new Document(variable.getKey(), object)),
                (result, exception) -> {
                    Utils.log.info("DatabaseAPI update() called ...");
                    if (exception == null && requestNew) {
                        requestGuild(guildName);
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
            case LAST_LOGOUT:
                return ((Document) PLAYERS.get(uuid).get("info")).get("lastLogout", Long.class);
            case IS_PLAYING:
                return ((Document) PLAYERS.get(uuid).get("info")).get("isPlaying", Boolean.class);
            case LEVEL:
                return ((Document) PLAYERS.get(uuid).get("info")).get("netLevel", Integer.class);
            case EXPERIENCE:
                return ((Document) PLAYERS.get(uuid).get("info")).get("experience", Double.class);
            case GEMS:
                return ((Document) PLAYERS.get(uuid).get("info")).get("gems", Integer.class);
            case HEARTHSTONE:
                return ((Document) PLAYERS.get(uuid).get("info")).get("hearthstone", String.class);
            case ECASH:
                return ((Document) PLAYERS.get(uuid).get("info")).get("ecash", Integer.class);
            case FRIENDS:
                return ((Document) PLAYERS.get(uuid).get("info")).get("friends", ArrayList.class);
            case GUILD:
                return ((Document) PLAYERS.get(uuid).get("info")).get("guild", String.class);
            case GUILD_INVITES:
                return ((Document) PLAYERS.get(uuid).get("notices")).get("guildInvites", ArrayList.class);
            case FRIEND_REQUSTS:
                return ((Document) PLAYERS.get(uuid).get("notices")).get("friendRequest", ArrayList.class);
            case MAILBOX:
                return ((Document) PLAYERS.get(uuid).get("notices")).get("mailbox", ArrayList.class);
            case ALIGNMENT:
                return ((Document) PLAYERS.get(uuid).get("info")).get("alignment", String.class);
            case CURRENT_LOCATION:
                return ((Document) PLAYERS.get(uuid).get("info")).get("currentLocation", String.class);
            case CURRENT_FOOD:
                return ((Document) PLAYERS.get(uuid).get("info")).get("foodLevel", Integer.class);
            case SHOPLEVEL:
                return ((Document) PLAYERS.get(uuid).get("info")).get("shopLevel", Integer.class);
            case LOGGERDIED:
                return ((Document) PLAYERS.get(uuid).get("info")).get("loggerdied", Boolean.class);

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
                return ((Document) PLAYERS.get(uuid).get("attributes")).get("strength", Integer.class);
            case DEXTERITY:
                return ((Document) PLAYERS.get(uuid).get("attributes")).get("dexterity", Integer.class);
            case INTELLECT:
                return ((Document) PLAYERS.get(uuid).get("attributes")).get("intellect", Integer.class);
            case VITALITY:
                return ((Document) PLAYERS.get(uuid).get("attributes")).get("vitality", Integer.class);
            case BUFFER_POINTS:
                return ((Document) PLAYERS.get(uuid).get("attributes")).get("bufferPoints", Integer.class);
            case RESETS:
                return ((Document) PLAYERS.get(uuid).get("attributes")).get("resets", Integer.class);
            case FREERESETS:
                return ((Document) PLAYERS.get(uuid).get("attributes")).get("freeresets", Integer.class);
            /*
            Player Storage
             */
            case INVENTORY_LEVEL:
                return ((Document) PLAYERS.get(uuid).get("inventory")).get("level", Integer.class);
            case INVENTORY_COLLECTION_BIN:
                return ((Document) PLAYERS.get(uuid).get("inventory")).get("collection_bin", String.class);
            case INVENTORY_MULE:
                return ((Document) PLAYERS.get(uuid).get("inventory")).get("mule", String.class);
            case INVENTORY_STORAGE:
                return ((Document) PLAYERS.get(uuid).get("inventory")).get("storage", String.class);
            case INVENTORY:
                return ((Document) PLAYERS.get(uuid).get("inventory")).get("player", String.class);
            case HASSHOP:
                return ((Document) PLAYERS.get(uuid).get("info")).get("shopOpen", Boolean.class);
            case ARMOR:
                return ((Document) PLAYERS.get(uuid).get("inventory")).get("armor", ArrayList.class);
            /*
            Toggles
             */
            case TOGGLE_DEBUG:
                return ((Document) PLAYERS.get(uuid).get("toggles")).get("debug", Boolean.class);
            case TOGGLE_TRADE:
                return ((Document) PLAYERS.get(uuid).get("toggles")).get("trade", Boolean.class);
            case TOGGLE_TRADE_CHAT:
                return ((Document) PLAYERS.get(uuid).get("toggles")).get("tradeChat", Boolean.class);
            case TOGGLE_GLOBAL_CHAT:
                return ((Document) PLAYERS.get(uuid).get("toggles")).get("globalChat", Boolean.class);
            case TOGGLE_RECEIVE_MESSAGE:
                return ((Document) PLAYERS.get(uuid).get("toggles")).get("receiveMessage", Boolean.class);
            case TOGGLE_PVP:
                return ((Document) PLAYERS.get(uuid).get("toggles")).get("pvp", Boolean.class);
            case TOGGLE_DUEL:
                return ((Document) PLAYERS.get(uuid).get("toggles")).get("duel", Boolean.class);
            case TOGGLE_CHAOTIC_PREVENTION:
                return ((Document) PLAYERS.get(uuid).get("toggles")).get("chaoticPrevention", Boolean.class);
            /*
            Portal Key Shards
             */
            case PORTAL_SHARDS_T1:
                return ((Document) PLAYERS.get(uuid).get("portalKeyShards")).get("tier1", Integer.class);
            case PORTAL_SHARDS_T2:
                return ((Document) PLAYERS.get(uuid).get("portalKeyShards")).get("tier2", Integer.class);
            case PORTAL_SHARDS_T3:
                return ((Document) PLAYERS.get(uuid).get("portalKeyShards")).get("tier3", Integer.class);
            case PORTAL_SHARDS_T4:
                return ((Document) PLAYERS.get(uuid).get("portalKeyShards")).get("tier4", Integer.class);
            case PORTAL_SHARDS_T5:
                return ((Document) PLAYERS.get(uuid).get("portalKeyShards")).get("tier5", Integer.class);
            /*
            Player Collectibles
             */
            case MOUNTS:
                return ((Document) PLAYERS.get(uuid).get("collectibles")).get("mounts", ArrayList.class);
            case PETS:
                return ((Document) PLAYERS.get(uuid).get("collectibles")).get("pets", ArrayList.class);
            case PARTICLES:
                return ((Document) PLAYERS.get(uuid).get("collectibles")).get("particles", ArrayList.class);
            case ACHIEVEMENTS:
                return ((Document) PLAYERS.get(uuid).get("collectibles")).get("achievements", ArrayList.class);
            default:
        }
        return null;
    }

    /**
     * Returns the object that's requested.
     *
     * @param data
     * @param guildName
     * @return
     * @since 1.0
     */
    public Object getData(EnumGuildData data, String guildName) {
        switch (data) {
            case NAME:
                return ((Document) GUILDS.get(guildName).get("info")).get("name", String.class);
            case MOTD:
                return ((Document) GUILDS.get(guildName).get("info")).get("motd", String.class);
            case CLAN_TAG:
                return ((Document) GUILDS.get(guildName).get("info")).get("clanTag", String.class);
            case OWNER:
                return ((Document) GUILDS.get(guildName).get("info")).get("owner", String.class);
            case CO_OWNER:
                return ((Document) GUILDS.get(guildName).get("info")).get("coOwner", String.class);
            case OFFICERS:
                return ((Document) GUILDS.get(guildName).get("info")).get("officers", ArrayList.class);
            case MEMBERS:
                return ((Document) GUILDS.get(guildName).get("info")).get("members", ArrayList.class);
            case CREATION_UNIX_DATA:
                return ((Document) GUILDS.get(guildName).get("info")).get("unixCreation");
            case INVITATIONS:
                return ((Document) GUILDS.get(guildName).get("info")).get("invitations", ArrayList.class);
            case ICON:
                return ((Document) GUILDS.get(guildName).get("info")).get("icon", String.class);
            /*
             * Levels
             */
            case LEVEL:
                return ((Document) GUILDS.get(guildName).get("info")).get("netLevel", Integer.class);
            case EXPERIENCE:
                return ((Document) GUILDS.get(guildName).get("info")).get("experience", Double.class);
            /*
            Boosters
             */
            case BOOSTERS_ACTIVE:
                return ((Document) GUILDS.get(guildName).get("boosters")).get("active", String.class);
            case BOOSTERS_AVAILABLE:
                return ((Document) GUILDS.get(guildName).get("available")).get("experience", ArrayList.class);
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
            if (document != null) {
                Utils.log.info("Fetched information for uuid: " + uuid.toString());
                PLAYERS.put(uuid, document);
            } else {
                addNewPlayer(uuid);
            }
        });
    }

    /**
     * Gets the requested GuildName and puts in Guild.YAVA
     * class.
     *
     * @param guildName
     * @since 1.0
     */
    public void requestGuild(String guildName) {
        Database.guilds.find(Filters.eq("info.name", guildName)).first((document, throwable) -> {
            if (document != null) {
                GUILDS.put(guildName, document);
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
                                .append("gems", 0)
                                .append("ecash", 0)
                                .append("firstLogin", System.currentTimeMillis() / 1000L)
                                .append("lastLogin", 0l)
                                .append("lastLogout", 0l)
                                .append("netLevel", 1)
                                .append("experience", 0d)
                                .append("hearthstone", "Cyrennica")
                                .append("currentLocation", "")
                                .append("isPlaying", true)
                                .append("friends", new ArrayList<>())
                                .append("alignment", "lawful")
                                .append("guild", "")
                                .append("shopOpen", false)
                                .append("foodLevel", 20)
                                .append("shopLevel", 1)
                                .append("loggerdied", false))
                        .append("attributes",
                                new Document("bufferPoints", 6)
                                        .append("strength", 0)
                                        .append("dexterity", 0)
                                        .append("intellect", 0)
                                        .append("vitality", 0)
                                        .append("resets", 0)
                                        .append("freeresets", 0))
                        .append("collectibles",
                                new Document("achievements", new ArrayList<String>())
                                        .append("mounts", new ArrayList<String>())
                                        .append("pets", new ArrayList<String>())
                                        .append("particles", new ArrayList<String>()))
                        .append("toggles",
                                new Document("debug", true)
                                        .append("trade", false)
                                        .append("tradeChat", false)
                                        .append("globalChat", false)
                                        .append("receiveMessage", true)
                                        .append("pvp", false)
                                        .append("duel", false)
                                        .append("chaoticPrevention", true))
                        .append("portalKeyShards",
                                new Document("tier1", 0)
                                        .append("tier2", 0)
                                        .append("tier3", 0)
                                        .append("tier4", 0)
                                        .append("tier5", 0))
                        .append("notices",
                                new Document("guildInvites", new ArrayList<String>())
                                        .append("friendRequest", new ArrayList<String>())
                                        .append("mailbox", new ArrayList<String>()))
                        .append("rank",
                                new Document("lastPurchase", 0l)
                                        .append("purchaseHistory", new ArrayList<String>())
                                        .append("rank", "DEFAULT"))
                        .append("inventory",
                                new Document("collection_bin", "")
                                        .append("mule", "empty")
                                        .append("storage", "")
                                        .append("level", 1)
                                        .append("player", "")
                                        .append("armor", new ArrayList<String>()));
        Database.collection.insertOne(newPlayerDocument, (aVoid, throwable) -> {
            requestPlayer(uuid);
            Utils.log.info("Requesting new data for : " + uuid);
        });
    }
}
