package net.dungeonrealms.database;


import com.mysql.jdbc.StatementImpl;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.game.util.StringUtils;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.util.TimeUtil;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.CurrencyTab;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.player.stats.PlayerStats;
import net.dungeonrealms.game.world.entity.type.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.dungeonrealms.game.world.entity.type.pet.PetData;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PlayerWrapper {

    @Getter
    private static Map<UUID, PlayerWrapper> playerWrappers = new ConcurrentHashMap<>();

//    private static final ExecutorService SQL_EXECUTOR_SERVICE =
//            Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("PlayerWrapper SQL Thread").build());

    @Getter
    private UUID uuid;

    @Getter
    private int accountID, characterID;

    @Getter
    @Setter
    private int guildID;

    @Getter
    private PlayerGameStats playerGameStats;

    @Getter
    @Setter
    private long timeCreated;

    @Getter
    @Setter
    private int health, level, ecash, experience;

    @Getter
    private int gems;

    @Getter
    @Setter
    private long lastLogin, lastLogout, lastShardTransfer, lastFreeEcash, firstLogin;

    @Getter
    private Inventory pendingInventory, pendingArmor, pendingMuleInventory;

    @Getter
    @Setter
    private String pendingInventoryString, pendingArmorString, storedLocationString, storedCollectionBinString;

    @Getter
    @Setter
    private String shardPlayingOn, questData, username;

    @Getter
    @Setter
    private String activeMount, activePet, activeTrail, activeMountSkin;

    @Getter
    @Setter
    private int bankLevel, shopLevel, muleLevel, storedFoodLevel;


    @Getter
    private boolean isPlaying = false;

    @Getter
    @Setter
    private boolean combatLogged = false, shopOpened = false, loggerDied = false;

    @Getter
    @Setter
    private String lastIP, hearthstone;

    @Getter
    @Setter
    private long lastTimeIPUsed;

    @Getter
    @Setter
    private boolean enteringRealm = false, uploadingRealm, upgradingRealm;

    @Getter
    @Setter
    private long lastRealmReset;

    @Getter
    @Setter
    private String realmTitle, realmDescription;

    @Getter
    @Setter
    private int realmTier = 1;

    @Getter
    @Setter
    private KarmaHandler.EnumPlayerAlignments playerAlignment = KarmaHandler.EnumPlayerAlignments.LAWFUL;

    @Getter
    @Setter
    private int alignmentTime = 0;

    @Getter
    @Setter
    private Location storedLocation;

    @Setter
    @Getter
    private CurrencyTab currencyTab;

    @Setter
    private String rank;

    @Getter
    @Setter
    private int portalShardsT1, portalShardsT2, portalShardsT3, portalShardsT4, portalShardsT5;

    @Getter
    @Setter
    private long muteExpire, banExpire;

    @Getter
    @Setter
    private String muteReason, banReason;

    @Getter
    @Setter
    private Integer whoBannedMeID, whoMutedMeID;

    @Getter
    @Setter
    private boolean firstTimePlaying = false;

    @Getter
    private HashMap<UUID, Integer> friendsList = new HashMap<>(), ignoredFriends = new HashMap<>(), pendingFriends = new HashMap<>();

    @Getter
    private Set<String> achievements = new HashSet<>();

    @Getter
    private HashSet<String> mountsUnlocked, particlesUnlocked, mountSkins, trails;

    @Getter
    private Map<EnumPets, PetData> petsUnlocked = new HashMap<>();


    @Getter
    @Setter
    private String playerName;

    @Getter
    @Setter
    private Integer rankExpiration;

    @Getter
    @Setter
    private String lastViewedBuild;

    @Getter
    @Setter
    private int lastNoteSize;

    @Getter
    @Setter
    private long lastVote;


    @Getter
    private PlayerToggles toggles;

    @Getter
    private PlayerStats playerStats;

    @Getter
    private Storage pendingBankStorage;

    @Setter
    @Getter
    private Player player;

    public PlayerWrapper(UUID uuid) {
        this.uuid = uuid;

    }

    @SuppressWarnings("unused")
    public PlayerWrapper(Player player) {
        this(player.getUniqueId());
        this.player = player;
    }


    public void loadData(boolean async) {
        this.loadData(async, null);
    }

    /**
     * Load the playerWrapper data, Must be thread safe.
     *
     * @param async    async this method
     * @param callback callback to call after its loaded.
     */
    public void loadData(boolean async, Consumer<PlayerWrapper> callback) {
        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> loadData(false, callback), SQLDatabaseAPI.SERVER_EXECUTOR_SERVICE);
            return;
        }

        try {

            long start = System.currentTimeMillis();
            @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(
                    "SELECT * FROM `users` LEFT JOIN `ranks` ON `users`.`account_id` = `ranks`.`account_id` " +
                            "LEFT JOIN `toggles` ON `users`.`account_id` = `toggles`.`account_id` " +
                            "LEFT JOIN `ip_addresses` ON `users`.`account_id` = `ip_addresses`.`account_id` " +
                            "LEFT JOIN `mail` ON `users`.`account_id` = `mail`.`account_id` " +
                            "LEFT JOIN `guild_members` ON `users`.`account_id` = `guild_members`.`account_id` " +
                            "LEFT JOIN `guilds` ON `guild_members`.`guild_id` = `guilds`.`guild_id` " +
                            "LEFT JOIN `characters` ON `characters`.`character_id` = `users`.`selected_character_id` " +
                            "LEFT JOIN `attributes` ON `characters`.`character_id` = `attributes`.`character_id` " +
                            "LEFT JOIN `realm` ON `characters`.`character_id` = `realm`.`character_id` " +
                            "LEFT JOIN `statistics` ON `characters`.`character_id` = `statistics`.`character_id` " +
                            "WHERE `users`.`uuid` = ?;");
            statement.setString(1, uuid.toString());

            ResultSet result = statement.executeQuery();
            if (result.first()) {
                this.accountID = result.getInt("users.account_id");
                this.toggles = new PlayerToggles(this.accountID);
                this.username = result.getString("users.username");
                this.isPlaying = result.getBoolean("users.is_online");
                this.shardPlayingOn = result.getString("users.currentShard");
                this.characterID = result.getInt("characters.character_id");
                this.health = result.getInt("characters.health");
                this.level = result.getInt("characters.level");

                this.username = result.getString("users.username");


                this.loadBanks(result);
                this.loadPlayerPendingInventory(result);
                this.loadPlayerPendingEquipment(result);
                this.loadMuleInventory(result);
                this.loadLocation(result);

                this.guildID = result.getInt("guilds.guild_id");
                System.out.println("The guildID: " + guildID);


                this.lastLogin = result.getLong("users.last_login");
                this.lastLogout = result.getLong("users.last_logout");
                this.lastShardTransfer = result.getLong("users.last_shard_transfer");

                this.lastViewedBuild = result.getString("users.lastViewedBuild");
                this.lastNoteSize = result.getInt("users.lastNoteSize");

                this.lastVote = result.getLong("users.lastVote");

                this.playerGameStats = new PlayerGameStats(characterID);
                this.playerGameStats.extractData(result);

                this.toggles.extractData(result);

                this.playerStats = new PlayerStats(uuid);
                this.playerStats.extractData(result);

                this.ecash = result.getInt("users.ecash");
                this.lastFreeEcash = result.getLong("users.last_free_ecash");
                this.gems = result.getInt("characters.gems");
                this.experience = result.getInt("characters.experience");

                //String activeMount, activePet, activeTrail, activeMountSkin;
                this.activeMount = result.getString("characters.activeMount");
                this.activePet = result.getString("characters.activePet");
                this.activeTrail = result.getString("characters.activeTrail");
                this.activeMountSkin = result.getString("characters.activeMountSkin");

                this.questData = result.getString("characters.questData");
                this.shopLevel = result.getInt("characters.shop_level");
                this.storedFoodLevel = result.getInt("characters.foodLevel");
                this.combatLogged = result.getBoolean("characters.combatLogged");
                this.achievements = StringUtils.deserializeSet(result.getString("characters.achievements"), ",");
                this.shopOpened = result.getBoolean("characters.shopOpened");
                this.loggerDied = result.getBoolean("characters.loggerDied");
                //We need to get the most updated last_used variable when pulling this..
                this.lastIP = result.getString("ip_addresses.ip_address");
                this.lastTimeIPUsed = result.getLong("ip_addresses.last_used");
                this.hearthstone = result.getString("characters.currentHearthStone");
                this.alignmentTime = result.getInt("characters.alignmentTime");

                this.currencyTab = new CurrencyTab(this.uuid).deserializeCurrencyTab(result.getString("users.currencyTab"));
                this.rank = result.getString("ranks.rank");
                this.rankExpiration = result.getInt("ranks.expiration");

                this.realmTitle = result.getString("realm.title");
                this.realmDescription = result.getString("realm.description");

                //enteringRealm = false, uploading, upgrading;
                this.enteringRealm = result.getBoolean("realm.enteringRealm");
                this.uploadingRealm = result.getBoolean("realm.uploading");
                this.upgradingRealm = result.getBoolean("realm.upgrading");
                this.realmTier = result.getInt("realm.tier");
                this.firstLogin = result.getLong("users.firstLogin");
                this.lastRealmReset = result.getLong("realm.lastReset");

                //Unlockables.
                this.loadUnlockables(result);

                this.portalShardsT1 = result.getInt("characters.portalShardsT1");
                this.portalShardsT2 = result.getInt("characters.portalShardsT2");
                this.portalShardsT3 = result.getInt("characters.portalShardsT3");
                this.portalShardsT4 = result.getInt("characters.portalShardsT4");
                this.portalShardsT5 = result.getInt("characters.portalShardsT5");

                this.timeCreated = result.getLong("characters.created");

                this.loadFriends();


                if (Constants.debug)
                    Bukkit.getLogger().info("Loaded " + this.username + "'s PlayerWrapper data in " + (System.currentTimeMillis() - start) + "ms.");


                if (callback != null) {
                    callback.accept(this);
                    return;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (callback != null)
            callback.accept(null);

        SQLDatabaseAPI.getInstance().addQuery(QueryType.SET_ONLINE_STATUS, 1, DungeonRealms.getShard().getPseudoName(), accountID);
    }

    public Rank.PlayerRank getPlayerRank() {
        return Rank.PlayerRank.getFromInternalName(getRank());
    }

    public String getRank() {
        if (rank == null) return "DEFAULT";
        return rank;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }


    public String getPetName(EnumPets pets) {
        PetData petData = this.petsUnlocked.get(pets);
        if (petData != null && petData.getPetName() != null) return petData.getPetName();
        return pets.getDisplayName();
    }

    public void loadUnlockables(ResultSet result) throws SQLException {
        this.mountsUnlocked = StringUtils.deserializeSet(result.getString("users.mounts"), ",");
        this.particlesUnlocked = StringUtils.deserializeSet(result.getString("users.particles"), ",");
        this.mountSkins = StringUtils.deserializeSet(result.getString("users.mountSkin"), ",");
        this.trails = StringUtils.deserializeSet(result.getString("users.trails"), ",");
        List<String> list = StringUtils.deserializeList(result.getString("users.pets"), ",");
        if (list != null) {
            for (String str : list) {
                String type;
                String name = null;
                if (str.contains("@")) {
                    type = str.split("@")[0];
                    name = str.split("@")[1];
                } else {
                    type = str;
                }

                EnumPets pets = EnumPets.getByName(type);
                if (pets == null) {
                    Bukkit.getLogger().info("Invalid pet type: " + str + " for " + getUsername());
                    continue;
                }

                this.petsUnlocked.put(pets, new PetData(name));
            }
        }
    }

    public String getSerializePetString() {
        if (this.petsUnlocked.isEmpty()) return null;
        StringBuilder builder = new StringBuilder();

        this.petsUnlocked.forEach((pet, data) -> builder.append(pet.getRawName()).append(data != null && data.getPetName() != null ? "@" + data.getPetName() : "").append(","));

        return builder.toString();
    }

    //tfw greg pays $10 for a burger from macers, 8.1 is decent on that flavor scale
    public void loadPunishment(boolean async, Consumer<Long> muteLoadedCallback) {
        SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_ALL_PUNISHMENTS.getQuery(this.accountID), async, result -> {
            try {
                while (result.next()) {
                    if (result.getString("type").equals("mute")) {
                        this.muteExpire = result.getLong("expiration");
                        this.muteReason = result.getString("reason");
                        this.whoMutedMeID = result.getInt("punisher_id");
                        if (muteLoadedCallback != null)
                            muteLoadedCallback.accept(this.muteExpire);
                    } else {
                        this.banExpire = result.getLong("expiration");
                        this.banReason = result.getString("reason");
                        this.whoBannedMeID = result.getInt("punisher_id");
                    }
                }
                result.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadFriends() throws SQLException {
        //Not too sure if this query is correct for what I am trying to do. Can not test atm because we have no data in the database. If it doesn't work I will fix it.
        @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(
                "SELECT friend_id, status FROM friends WHERE account_id = ?;");
        statement.setInt(1, this.accountID);
        ResultSet result = statement.executeQuery();
        while (result.next()) {
            int friendId = result.getInt("friends.friend_id");
            String status = result.getString("friends.status");
            UUID friendUUID = SQLDatabaseAPI.getInstance().getUUIDFromAccountID(friendId);
            if (friendUUID == null) {
                Bukkit.getLogger().info("Unable to get friend UUID for ID " + friendId + " for " + this.username);
                continue;
            }

            if (status.equalsIgnoreCase("friends")) {
                friendsList.put(friendUUID, friendId);
            } else if (status.equalsIgnoreCase("pending")) {
                pendingFriends.put(friendUUID, friendId);
            } else if (status.equalsIgnoreCase("blocked")) {
                ignoredFriends.put(friendUUID, friendId);
            }
        }
    }


    @SneakyThrows
    private void loadLocation(ResultSet set) {
        this.storedLocationString = set.getString("characters.location");
        if (storedLocationString == null || storedLocationString.isEmpty() || storedLocationString.equalsIgnoreCase("null")) {
            firstTimePlaying = true;
        }
        if (!firstTimePlaying) {
            try {
                String[] locArray = storedLocationString.split(",");
                double x = Double.parseDouble(locArray[0]);
                double y = Double.parseDouble(locArray[1]);
                double z = Double.parseDouble(locArray[2]);
                float yaw = Float.parseFloat(locArray[3]);
                float pitch = Float.parseFloat(locArray[4]);
                //Success.
                this.storedLocation = new Location(Bukkit.getWorlds().get(0), x, y, z, yaw, pitch);
                return;
            } catch (Exception e) {
                Bukkit.getLogger().info("Unable to load location for " + storedLocationString);
                e.printStackTrace();
                //Rip.
            }
        }
        this.storedLocation = TeleportLocation.CYRENNICA.getLocation();
    }

    private String getLocationString(Player player) {
        if (!GameAPI.isMainWorld(player.getWorld())) {
            return storedLocationString;
        }
        return new StringBuilder().append(player.getLocation().getX()).append(',').append(player.getLocation().getY() + 0.3).append(',').append(player.getLocation().getZ()).append(',').append(player.getLocation().getYaw()).append(',').append(player.getLocation().getPitch()).toString();
    }

    public String getFormattedShardName() {
        if (!isPlaying()) return "None";
        ShardInfo shard = ShardInfo.getByPseudoName(getShardPlayingOn());
        return shard != null ? shard.getShardID() : "";
    }

    public void setPlayingStatus(boolean playing) {
        if (playing == this.isPlaying) return;
        this.isPlaying = playing;

        SQLDatabaseAPI.getInstance().executeUpdate(updates -> {
            //Set...
        }, QueryType.SET_ONLINE_STATUS.getQuery(this.isPlaying ? 1 : 0, this.isPlaying ? DungeonRealms.getShard().getPseudoName() : null, accountID));
//        CompletableFuture.runAsync(() -> {
//            try {
//                PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(
//                        QueryType.SET_ONLINE_STATUS.getQuery(0, this.isPlaying ? DungeonRealms.getShard().getPseudoName() : null, uuid.toString()));
////                        "UPDATE users SET is_online = ?, currentShard = ?,  WHERE `users`.`uuid` = ?;");
////                statement.setBoolean(1, this.isPlaying);
////                statement.setString(2, this.isPlaying ? DungeonRealms.getShard().getShardID() : null);
////                statement.setString(3, uuid.toString());
//                statement.executeUpdate();
//                statement.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }, SQLDatabaseAPI.SERVER_EXECUTOR_SERVICE);
    }

    public void saveData(boolean async, Boolean isOnline) {
        this.saveData(async, isOnline, null);
    }


    public void saveData(boolean async, Boolean isOnline, Consumer<PlayerWrapper> callback) {
        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> saveData(false, isOnline, callback), SQLDatabaseAPI.SERVER_EXECUTOR_SERVICE);
            return;
        }


        try {
            @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("");

            statement.addBatch(getCharacterReplaceQuery(player));
            //User query.
            statement.addBatch(getUsersUpdateQuery(isOnline));
            String playerStats = getPlayerStats().getUpdateStatement();
            statement.addBatch(playerStats);
            Bukkit.getLogger().info("Player Stats: " + playerStats);
            statement.addBatch(getToggles().getUpdateStatement());
            if (hasFriendData()) getFriendUpdateQuery(statement);
            if (this.player != null) {
                statement.addBatch(String.format("REPLACE INTO ip_addresses (account_id, ip_address, last_used) VALUES ('%s', '%s', '%s')", getAccountID(), player.getAddress().getAddress().getHostAddress(), System.currentTimeMillis()));
            }
            statement.addBatch(String.format("REPLACE INTO ranks (account_id, rank, expiration) VALUES ('%s', '%s', '%s')", getAccountID(), getRank().toUpperCase(), getRankExpiration()));
            //Realm info
            statement.addBatch(QueryType.UPDATE_REALM.getQuery(getRealmTitle(), getRealmDescription(), isUploadingRealm() ? 1 : 0, isUpgradingRealm() ? 1 : 0, getRealmTier(), isEnteringRealm() ? 1 : 0, getLastRealmReset(), getCharacterID()));

            long start = System.currentTimeMillis();
            Bukkit.getLogger().info("Preparing to execute batch: " + toString(((StatementImpl) statement).getBatchedArgs()));
            statement.executeBatch();
            Bukkit.getLogger().info("Batch executed in " + (System.currentTimeMillis() - start));
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (callback != null) {
            //System.out.println("Save data debug 15! isAsync: " + async + " isOnline: " + isOnline + " hasCallback: " + (callback != null));
            callback.accept(this);
        }
    }

    private String toString(List<Object> objects) {
        StringBuilder builder = new StringBuilder();
        for (Object object : objects) {
            builder.append(object != null ? object.toString() : "null").append(", ");
        }
        return builder.toString();
    }

    private String getCharacterReplaceQuery(Player player) {
        Storage bankStorage = BankMechanics.getInstance().getStorage(uuid);
        //Doesnt exist, offline data?
        if (bankStorage == null && player == null)
            bankStorage = pendingBankStorage;

        String collectionBinString = null;
        if (bankStorage != null && bankStorage.collection_bin != null)
            collectionBinString = ItemSerialization.toString(bankStorage.collection_bin);

        String bankString = bankStorage != null && bankStorage.inv != null ? ItemSerialization.toString(bankStorage.inv) : null;

        Inventory mule = MountUtils.inventories.get(this.uuid);
        if (mule == null && player == null)
            mule = pendingMuleInventory;

        String muleString = null;
        if (mule != null) muleString = ItemSerialization.toString(mule);


        String locationString = player == null ? storedLocationString : getLocationString(player);
        //33 total
        return QueryType.CHARACTER_UPDATE.getQuery(getTimeCreated(), getLevel(), getExperience(), getPlayerAlignment().name(), player == null ? this.pendingInventoryString : ItemSerialization.toString(player.getInventory()), player == null ? this.pendingArmorString : getEquipmentString(player), getGems(), bankString, getBankLevel(),
                getShopLevel(), muleString, getMuleLevel(), getHealth(), locationString,
                quote(getActiveMount()), quote(getActivePet()), quote(getActiveTrail()), quote(getActiveMountSkin()),
                quote(getQuestData()), quote(collectionBinString), player == null ? storedFoodLevel : player.getFoodLevel(), isCombatLogged() ? 1 : 0,
                isShopOpened() ? 1 : 0, isLoggerDied() ? 1 : 0, getHearthstone(), getAlignmentTime(), getPortalShardsT1(), getPortalShardsT2(), getPortalShardsT3(), getPortalShardsT4(), getPortalShardsT5(), getCharacterID());
    }


    private String quote(String string) {
        if (string == null) return null;
        return "'" + string + "'";
    }

    private String getUsersUpdateQuery(Boolean isOnline) {
        if (isOnline == null) isOnline = isPlaying();

        return QueryType.USER_UPDATE.getQuery(getUsername(), getCharacterID(), getEcash(), getTimeCreated(), getLastLogin(), getLastLogout(), getLastFreeEcash(), getLastShardTransfer(), isOnline ? 1 : 0, this.isPlaying ? DungeonRealms.getShard().getPseudoName() : "null", this.currencyTab == null ? null : getCurrencyTab().getSerializedScrapTab(), getFirstLogin(), getLastViewedBuild(), getLastNoteSize(), getLastVote(),
                StringUtils.serializeList(getMountsUnlocked(), ",", true), quote(getSerializePetString()), StringUtils.serializeList(getParticlesUnlocked(), ",", true), StringUtils.serializeList(getMountSkins(), ",", true), StringUtils.serializeList(getTrails(), ",", true), getAccountID());
    }

    @SneakyThrows
    private boolean getFriendUpdateQuery(PreparedStatement statement) {
        if (friendsList.size() == 0 && ignoredFriends.size() == 0 && pendingFriends.size() == 0) return false;

        StringBuilder builder = new StringBuilder("");
//        StringBuilder toReturn = new StringBuilder("REPLACE INTO friends (account_id, friend_id, status) VALUES ");
        for (int friendID : friendsList.values()) {
            statement.addBatch(QueryType.INSERT_FRIENDS.getQuery(getAccountID(), friendID, "friends", "friends"));
//            if (!isFirstValues) toReturn.append(", ");
//            toReturn.append('(');
//            toReturn.append(getAccountID());
//            toReturn.append(',');
//            toReturn.append(friendID);
//            toReturn.append(',');
//            toReturn.append("friends");
//            toReturn.append(')');
//            isFirstValues = false;
        }

        for (int friendID : ignoredFriends.values()) {
            statement.addBatch(QueryType.INSERT_FRIENDS.getQuery(getAccountID(), friendID, "blocked", "blocked"));
//            if (!isFirstValues) toReturn.append(", ");
//            toReturn.append('(');
//            toReturn.append(getAccountID());
//            toReturn.append(',');
//            toReturn.append(friendID);
//            toReturn.append(',');
//            toReturn.append("blocked");
//            toReturn.append(')');
//            isFirstValues = false;
        }

        for (int friendID : pendingFriends.values()) {
            statement.addBatch(QueryType.INSERT_FRIENDS.getQuery(getAccountID(), friendID, "pending", "pending"));
//            if (!isFirstValues) toReturn.append(", ");
//            toReturn.append('(');
//            toReturn.append(getAccountID());
//            toReturn.append(',');
//            toReturn.append(friendID);
//            toReturn.append(',');
//            toReturn.append("pending");
//            toReturn.append(')');
//            isFirstValues = false;
        }

        return true;
    }

    private boolean hasFriendData() {
        return !friendsList.isEmpty() || !ignoredFriends.isEmpty() || !pendingFriends.isEmpty();
    }

    public static PlayerWrapper getPlayerWrapper(Player toGet) {
        return getPlayerWrapper(toGet.getUniqueId());
    }

    /**
     * Callback will be async if we had to fetch the profile from the database.
     *
     * @param uuid
     * @param hadToLoadCallback
     */
    public static void getPlayerWrapper(UUID uuid, boolean storeWrapper, boolean getIfCached, Consumer<PlayerWrapper> hadToLoadCallback) {
        PlayerWrapper wrapper;
        if (getIfCached) {
            wrapper = getPlayerWrapper(uuid);
            if (wrapper != null) {
                if (hadToLoadCallback != null) {
                    hadToLoadCallback.accept(wrapper);
                    return;
                }
            }
        }

        //Load offline playerwrapper..
        wrapper = new PlayerWrapper(uuid);
        //Store that shit..
        if (storeWrapper) {
            Bukkit.getLogger().info("Storing player wrapper for " + uuid.toString());
            PlayerWrapper.setWrapper(uuid, wrapper);
        }
        Bukkit.getLogger().info("Loading " + uuid.toString() + "'s offline wrapper.");
        wrapper.loadData(true, hadToLoadCallback);
    }

    public static void getPlayerWrapper(UUID uuid, Consumer<PlayerWrapper> hadToLoadCallback) {
        getPlayerWrapper(uuid, true, true, hadToLoadCallback);
    }

    public static PlayerWrapper getPlayerWrapper(UUID toGet) {
        return playerWrappers.get(toGet);
    }

    public static void setWrapper(UUID uuid, PlayerWrapper wrapper) {
        playerWrappers.put(uuid, wrapper);
    }


    @SneakyThrows
    private void loadBanks(ResultSet set) {
        setBankLevel(set.getInt("characters.bank_level"));
        String serializedStorage = set.getString("characters.bank_storage");
        if (serializedStorage == null) {
            this.pendingBankStorage = new Storage(uuid, this.characterID);
            return;
        }

        //Auto set the inventory size based off level? min 9, max 54
        Inventory inv = ItemSerialization.fromString(serializedStorage, Math.max(9, Math.min(54, getBankLevel() * 9)));
        this.pendingBankStorage = new Storage(uuid, inv, this.characterID);
        loadCollectionBin(set, this.pendingBankStorage);

//        BankMechanics.storage.put(uuid, storageTemp);
    }

    @SneakyThrows
    private void loadCollectionBin(ResultSet set, Storage storage) {
        String stringInv = set.getString("characters.collection_storage");
        if (stringInv != null && stringInv.length() > 1) {
            Inventory inv = ItemSerialization.fromString(stringInv);
            if (inv == null) return;
            for (ItemStack item : inv.getContents())
                if (item != null && item.getType() == Material.AIR)
                    inv.addItem(item);

            storage.collection_bin = inv;
        }

    }

    @SneakyThrows
    private void loadPlayerPendingInventory(ResultSet set) {
        this.pendingInventoryString = set.getString("characters.inventory_storage");
        if (pendingInventoryString != null && pendingInventoryString.length() > 0 && !pendingInventoryString.equalsIgnoreCase("null")) {
            pendingInventory = ItemSerialization.fromString(pendingInventoryString, 36);
        }
    }

    @SneakyThrows
    private void loadPlayerPendingEquipment(ResultSet set) {
        this.pendingArmorString = set.getString("characters.armour_storage");
        if (pendingArmorString != null && pendingArmorString.length() > 0 && !pendingArmorString.equalsIgnoreCase("null")) {
            pendingArmor = ItemSerialization.fromString(pendingArmorString, 9);
        }

    }

    @SneakyThrows
    public void loadMuleInventory(ResultSet set) {
        String invString = set.getString("characters.mule_storage");
        muleLevel = set.getInt("characters.mule_level");
        if (muleLevel > 3) {
            muleLevel = 3;
        }
        MuleTier tier = MuleTier.getByTier(muleLevel);
        if (tier != null) {
            this.pendingMuleInventory = Bukkit.createInventory(null, tier.getSize(), "Mule Storage");
            if (invString != null && !invString.equalsIgnoreCase("") && !invString.equalsIgnoreCase("empty") && invString.length() > 4) {
                //Make sure the inventory is as big as we need
                this.pendingMuleInventory = ItemSerialization.fromString(invString, tier.getSize());
            }
        }
    }

    private String getEquipmentString(Player player) {

        int itemsSaved = 0;
        Inventory toSave = Bukkit.createInventory(null, 9);
        for (int index = 0; index < 4; index++) {
            ItemStack equipment = player.getEquipment().getArmorContents()[index];
//            toSave.getContents()[index] = equipment;
            toSave.setItem(index, equipment);
            if (equipment != null && equipment.getType() != Material.AIR) {
                itemsSaved++;
            }
        }

        if (player.getInventory().getItemInOffHand() != null && player.getInventory().getItemInOffHand().getType() != Material.AIR)
            itemsSaved++;

        toSave.setItem(4, player.getInventory().getItemInOffHand());
//        toSave.getContents()[4] = player.getInventory().getItemInOffHand();

//        Bukkit.getLogger().info("Equipment: " + );
        return itemsSaved == 0 ? null : ItemSerialization.toString(toSave);
    }

    public String getEquipmentString(Inventory inv) {
        int itemsSaved = 0;
        Inventory toSave = Bukkit.createInventory(null, 9);
        for (int index = 0; index < 5; index++) {
            ItemStack equipment = inv.getContents()[index];
            toSave.getContents()[index] = equipment;
            if (equipment != null && equipment.getType() != Material.AIR) {
                itemsSaved++;
            }
        }

        return itemsSaved == 0 ? null : ItemSerialization.toString(toSave);
    }

    public void loadPlayerAfterLogin(Player player) {
        this.player = player;
        player.teleport(this.storedLocation);
        this.loadPlayerInventory(player);
        this.loadPlayerArmor(player);

        //Only apply this storages to the cache when they join and are online...
        BankMechanics.storage.put(uuid, this.pendingBankStorage);
        //Actually apply this on login.
        if (this.pendingMuleInventory != null)
            MountUtils.inventories.put(uuid, pendingMuleInventory);

    }

    public void loadPlayerInventory(Player player) {
        if (pendingInventory == null) return;
        player.getInventory().setContents(pendingInventory.getContents());
        pendingInventory = null;
    }

    public void loadPlayerArmor(Player player) {
        if (pendingArmor == null) return;
        ItemStack[] items = new ItemStack[4];
        for (int index = 0; index < 4; index++) {
            //We are doing 5 for the new shield slot.
            ItemStack current = pendingArmor.getContents()[index];

            items[index] = current;
        }
        player.getEquipment().setArmorContents(items);
        player.getInventory().setItemInOffHand(pendingArmor.getContents()[4]);

        player.updateInventory();
    }

    /**
     * FRIENDS STUFF
     **/

    public void ignorePlayer(UUID uuidToIgnore, boolean alreadyIgnored) {
        int accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuidToIgnore);
        if (alreadyIgnored) {
            this.ignoredFriends.remove(uuidToIgnore);
            SQLDatabaseAPI.getInstance().addQuery(QueryType.INSERT_FRIENDS, this.accountID, accountID, "blocked", "blocked");
        } else {
            this.pendingFriends.remove(uuidToIgnore);
            this.ignoredFriends.put(uuidToIgnore, accountID);
            this.friendsList.remove(uuidToIgnore);
            SQLDatabaseAPI.getInstance().addQuery(QueryType.INSERT_FRIENDS, this.accountID, accountID, "blocked", "blocked");

        }
    }

    public void saveFriends(boolean async, Consumer<Boolean> afterSave) {
        if (friendsList.size() == 0 && ignoredFriends.size() == 0 && pendingFriends.size() == 0) return;
        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> saveFriends(false, afterSave), SQLDatabaseAPI.SERVER_EXECUTOR_SERVICE);
            return;
        }

        boolean couldSave = true;
        try {
            @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("");
            getFriendUpdateQuery(statement);
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            couldSave = false;
        }

        if (afterSave != null) afterSave.accept(couldSave);
    }

    public void setGems(int gems) {
        if (gems <= 0) gems = 0;
        this.gems = gems;
    }

    public boolean isBanned() {
        return this.banExpire >= System.currentTimeMillis() || this.banExpire == 0;
    }

    public boolean isMuted() {
        return this.muteExpire >= System.currentTimeMillis() || this.muteExpire == 0;
    }

    public String getTimeWhenBanExpires() {
        return TimeUtil.formatDifference((this.banExpire - System.currentTimeMillis()) / 1_000);
    }

    public String getTimeWhenMuteExpires() {
        return TimeUtil.formatDifference((this.muteExpire - System.currentTimeMillis()) / 1_000);
    }


}
