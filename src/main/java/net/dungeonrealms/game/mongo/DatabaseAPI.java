package net.dungeonrealms.game.mongo;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.mastery.Utils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nick on 8/29/2015.
 */
public class DatabaseAPI {

    private static DatabaseAPI instance = null;
    public volatile ConcurrentHashMap<UUID, Document> PLAYERS = new ConcurrentHashMap<>();
    public volatile ConcurrentHashMap<UUID, Integer> PLAYER_TIME = new ConcurrentHashMap<>();

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
     * @since 1.0
     */
    public void update(UUID uuid, EnumOperators EO, EnumData variable, Object object, boolean requestNew) {
        Database.collection.updateOne(Filters.eq("info.uuid", uuid.toString()), new Document(EO.getUO(), new Document(variable.getKey(), object)));
        if (requestNew) {
            requestPlayer(uuid);
        }
                /*(result, exception) -> {
                    Utils.log.info("DatabaseAPI update() called ...");
                    if (exception == null && requestNew) {
                        requestPlayer(uuid);
                    }
                });*/
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
            doc = Database.collection.find(Filters.eq("info.uuid", uuid.toString())).first();
        }

        switch (data) {
            /*
            Player Variables Main Document().
             */
            case USERNAME:
                return ((Document) doc.get("info")).get("username", String.class);
            case HEALTH:
                return ((Document) doc.get("info")).get("health", Integer.class);
            case FIRST_LOGIN:
                return ((Document) doc.get("info")).get("firstLogin", Long.class);
            case LAST_LOGIN:
                return ((Document) doc.get("info")).get("lastLogin", Long.class);
            case LAST_LOGOUT:
                return ((Document) doc.get("info")).get("lastLogout", Long.class);
            case FREE_ECASH:
                return ((Document) doc.get("info")).get("freeEcash", Long.class);
            case LAST_SHARD_TRANSFER:
                return ((Document) doc.get("info")).get("lastShardTransfer", Long.class);
            case IS_PLAYING:
                return ((Document) doc.get("info")).get("isPlaying", Boolean.class);
            case IS_SWITCHING_SHARDS:
                return ((Document) doc.get("info")).get("isSwitchingShards", Boolean.class);
            case LEVEL:
                return ((Document) doc.get("info")).get("netLevel", Integer.class);
            case EXPERIENCE:
                return ((Document) doc.get("info")).get("experience", Integer.class);
            case GEMS:
                return ((Document) doc.get("info")).get("gems", Integer.class);
            case HEARTHSTONE:
                return ((Document) doc.get("info")).get("hearthstone", String.class);
            case ECASH:
                return ((Document) doc.get("info")).get("ecash", Integer.class);
            case FRIENDS:
                return ((Document) doc.get("info")).get("friends", ArrayList.class);
            case GUILD:
                return ((Document) doc.get("info")).get("guild", String.class);
            case GUILD_INVITATION:
                return ((Document) doc.get("notices")).get("guildInvitation", Document.class);
            case FRIEND_REQUSTS:
                return ((Document) doc.get("notices")).get("friendRequest", ArrayList.class);
            case MAILBOX:
                return ((Document) doc.get("notices")).get("mailbox", ArrayList.class);
            case ALIGNMENT:
                return ((Document) doc.get("info")).get("alignment", String.class);
            case ALIGNMENT_TIME:
                return ((Document) doc.get("info")).get("alignmentTime", Integer.class);
            case CURRENT_LOCATION:
                return ((Document) doc.get("info")).get("currentLocation", String.class);
            case CURRENT_FOOD:
                return ((Document) doc.get("info")).get("foodLevel", Integer.class);
            case SHOPLEVEL:
                return ((Document) doc.get("info")).get("shopLevel", Integer.class);
            case MULELEVEL:
                return ((Document) doc.get("info")).get("muleLevel", Integer.class);
            case LOGGERDIED:
                return ((Document) doc.get("info")).get("loggerdied", Boolean.class);
            case CURRENTSERVER:
                return ((Document) doc.get("info")).get("current", String.class);
            case ENTERINGREALM:
                return ((Document) doc.get("info")).get("enteringrealm", String.class);
            case ACTIVE_MOUNT:
                return ((Document) doc.get("info")).get("activemount", String.class);
            case ACTIVE_PET:
                return ((Document) doc.get("info")).get("activepet", String.class);
            case ACTIVE_TRAIL:
                return ((Document) doc.get("info")).get("activetrail", String.class);
            case ACTIVE_MOUNT_SKIN:
                return ((Document) doc.get("info")).get("activemountskin", String.class);
            /*
            Rank Things. Different Sub-Document().
             */
            case RANK:
                return ((Document) doc.get("rank")).get("rank", String.class);
            case RANK_SUB_EXPIRATION:
                return ((Document) doc.get("rank")).get("expiration_date", Integer.class);
            case PURCHASE_HISTORY:
                return ((Document) doc.get("rank")).get("purchaseHistory", ArrayList.class);
            /*
            Player Attribute Variables
             */
            case STRENGTH:
                return ((Document) doc.get("attributes")).get("strength", Integer.class);
            case DEXTERITY:
                return ((Document) doc.get("attributes")).get("dexterity", Integer.class);
            case INTELLECT:
                return ((Document) doc.get("attributes")).get("intellect", Integer.class);
            case VITALITY:
                return ((Document) doc.get("attributes")).get("vitality", Integer.class);
            case BUFFER_POINTS:
                return ((Document) doc.get("attributes")).get("bufferPoints", Integer.class);
            case RESETS:
                return ((Document) doc.get("attributes")).get("resets", Integer.class);
            case FREERESETS:
                return ((Document) doc.get("attributes")).get("freeresets", Integer.class);
            /*
              PUNISH
             */
            case BANNED_TIME:
                return ((Document) doc.get("punishments")).get("banned", Long.class);
            case MUTE_TIME:
                return ((Document) doc.get("punishments")).get("muted", Long.class);
            case BANNED_REASON:
                return ((Document) doc.get("punishments")).get("banReason", String.class);
            case MUTE_REASON:
                return ((Document) doc.get("punishments")).get("muteReason", String.class);
            /*
              REALMS
             */
            case REALM_TITLE:
                return ((Document) doc.get("realm")).get("title", String.class);
            case REALM_UPLOAD:
                return ((Document) doc.get("realm")).get("uploading", Boolean.class);
            case REALM_UPGRADE:
                return ((Document) doc.get("realm")).get("upgrading", Boolean.class);
            case REALM_LAST_RESET:
                return ((Document) doc.get("realm")).get("lastReset", Long.class);
            case REALM_TIER:
                return ((Document) doc.get("realm")).get("tier", Integer.class);
            /*
            Player Storage
             */
            case INVENTORY_LEVEL:
                return ((Document) doc.get("inventory")).get("level", Integer.class);
            case INVENTORY_COLLECTION_BIN:
                return ((Document) doc.get("inventory")).get("collection_bin", String.class);
            case INVENTORY_MULE:
                return ((Document) doc.get("inventory")).get("mule", String.class);
            case INVENTORY_STORAGE:
                return ((Document) doc.get("inventory")).get("storage", String.class);
            case INVENTORY:
                return ((Document) doc.get("inventory")).get("player", String.class);
            case HASSHOP:
                return ((Document) doc.get("info")).get("shopOpen", Boolean.class);
            case ARMOR:
                return ((Document) doc.get("inventory")).get("armor", ArrayList.class);
            case ITEMUIDS:
                return ((Document) doc.get("inventory")).get("itemuids", HashSet.class);
            /*
            Toggles
             */
            case TOGGLE_DEBUG:
                return ((Document) doc.get("toggles")).get("debug", Boolean.class);
            case TOGGLE_TRADE:
                return ((Document) doc.get("toggles")).get("trade", Boolean.class);
            case TOGGLE_TRADE_CHAT:
                return ((Document) doc.get("toggles")).get("tradeChat", Boolean.class);
            case TOGGLE_GLOBAL_CHAT:
                return ((Document) doc.get("toggles")).get("globalChat", Boolean.class);
            case TOGGLE_RECEIVE_MESSAGE:
                return ((Document) doc.get("toggles")).get("receiveMessage", Boolean.class);
            case TOGGLE_PVP:
                return ((Document) doc.get("toggles")).get("pvp", Boolean.class);
            case TOGGLE_DUEL:
                return ((Document) doc.get("toggles")).get("duel", Boolean.class);
            case TOGGLE_CHAOTIC_PREVENTION:
                return ((Document) doc.get("toggles")).get("chaoticPrevention", Boolean.class);
            case TOGGLE_TIPS:
                return ((Document) doc.get("toggles")).get("tips", Boolean.class);
            /*
            Portal Key Shards
             */
            case PORTAL_SHARDS_T1:
                return ((Document) doc.get("portalKeyShards")).get("tier1", Integer.class);
            case PORTAL_SHARDS_T2:
                return ((Document) doc.get("portalKeyShards")).get("tier2", Integer.class);
            case PORTAL_SHARDS_T3:
                return ((Document) doc.get("portalKeyShards")).get("tier3", Integer.class);
            case PORTAL_SHARDS_T4:
                return ((Document) doc.get("portalKeyShards")).get("tier4", Integer.class);
            case PORTAL_SHARDS_T5:
                return ((Document) doc.get("portalKeyShards")).get("tier5", Integer.class);
            /*
            Player Collectibles
             */
            case MOUNTS:
                return ((Document) doc.get("collectibles")).get("mounts", ArrayList.class);
            case PETS:
                return ((Document) doc.get("collectibles")).get("pets", ArrayList.class);
            case PARTICLES:
                return ((Document) doc.get("collectibles")).get("particles", ArrayList.class);
            case ACHIEVEMENTS:
                return ((Document) doc.get("collectibles")).get("achievements", ArrayList.class);
            case MOUNT_SKINS:
                return ((Document) doc.get("collectibles")).get("mountskins", ArrayList.class);
            /*
            Player Statistics
             */
            case PLAYER_KILLS:
            case LAWFUL_KILLS:
            case UNLAWFUL_KILLS:
            case DEATHS:
            case T1_MOB_KILLS:
            case T2_MOB_KILLS:
            case T3_MOB_KILLS:
            case T4_MOB_KILLS:
            case T5_MOB_KILLS:
            case BOSS_KILLS_MAYEL:
            case BOSS_KILLS_BURICK:
            case BOSS_KILLS_INFERNALABYSS:
            case LOOT_OPENED:
            case DUELS_WON:
            case DUELS_LOST:
            case ORE_MINED:
            case ORBS_USED:
            case FISH_CAUGHT:
            case TIME_PLAYED:
            case SUCCESSFUL_ENCHANTS:
            case FAILED_ENCHANTS:
            case ECASH_SPENT:
            case GEMS_EARNED:
            case GEMS_SPENT:
                String data_key = data.getKey();
                if (data_key.contains(".")) {
                    data_key = data.getKey().substring(data.getKey().lastIndexOf(".") + 1, data.getKey().length());
                }
                return ((Document) doc.get("stats")).get(data_key, Integer.class);
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
        Document doc = Database.collection.find(Filters.eq("info.uuid", uuid.toString())).first();
        if (doc == null) {
            addNewPlayer(uuid);
        } else {
            PLAYERS.put(uuid, doc);
        }

        GuildMechanics.getInstance().checkPlayerGuild(uuid);
        /*Database.collection.find(Filters.eq("info.uuid", uuid.toString())).first((document) -> {
            if (document != null) {
                Utils.log.info("Fetched information for uuid: " + uuid.toString());
                PLAYERS.put(uuid, document);
            } else {
                addNewPlayer(uuid);
            }
        });*/
    }

    public Object getValue(UUID uuid, EnumData data) {
        Document doc = Database.collection.find(Filters.eq("info.uuid", uuid.toString())).first();
        if (doc == null) return null;
        String[] key = data.getKey().split("\\.");
        return ((Document) doc.get(key[0])).get(key[1]);
    }

    public String getUUIDFromName(String playerName) {
        Document doc = Database.collection.find(Filters.eq("info.username", playerName.toLowerCase())).first();
        if (doc == null) return "";
        return ((Document) doc.get("info")).get("uuid", String.class);
    }

    public String getFormattedShardName(UUID uuid) {
        Document doc = Database.collection.find(Filters.eq("info.uuid", uuid.toString())).first();
        if (doc == null)
            return "";
        String name = ((Document) doc.get("info")).get("current", String.class);
        return name.split("(?=[0-9])", 2)[0].toUpperCase() + "-" + name.split("(?=[0-9])", 2)[1];
    }

    public String getOfflineName(UUID uuid) {
        Document doc = Database.collection.find(Filters.eq("info.uuid", uuid.toString())).first();
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
                                .append("firstLogin", System.currentTimeMillis() / 1000L)
                                .append("lastLogin", 0L)
                                .append("lastLogout", 0L)
                                .append("freeEcash", 0L)
                                .append("isSwitchingShards", false)
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
                                .append("current", DungeonRealms.getInstance().bungeeName)
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
        Database.collection.insertOne(newPlayerDocument);
        requestPlayer(uuid);
        Utils.log.info("Requesting new data for : " + uuid);
    }
}
