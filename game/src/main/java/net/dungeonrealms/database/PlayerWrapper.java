package net.dungeonrealms.database;


import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.world.entity.type.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PlayerWrapper {

    private static Map<UUID, PlayerWrapper> playerWrappers = new HashMap<>();

    private static final ExecutorService SQL_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("SQL Thread").build());

    @Getter
    private UUID uuid;

    @Getter
    private int accountID, characterID;

    @Getter
    private PlayerStats playerStats;

    @Getter
    @Setter
    private int health, level, ecash, experience, gems;

    @Getter
    private long lastLogin, lastLogout, lastShardTransfer, lastFreeEcash;

    @Getter
    private Inventory pendingInventory, pendingArmor;

    @Getter
    @Setter
    String shardPlayingOn, questData;

    @Getter
    @Setter
    String activeMount, activePet, activeTrail, activeMountSkin;

    @Getter
    @Setter
    private int bankLevel, shopLevel, muleLevel, storedFoodLevel;


    @Getter
    private boolean isPlaying = false, combatLogged = false, shopOpened = false, loggerDied = false, enteringRealm = false;
    @Getter
    String ipAddress, hearthstone;

    @Getter
    @Setter
    private KarmaHandler.EnumPlayerAlignments alignment;

    @Getter
    @Setter
    private int alignmentTime = 0;

    @Getter
    @Setter
    private Location storedLocation;

    private List<Integer> friendsList = Lists.newArrayList(), ignored = Lists.newArrayList(), pending = Lists.newArrayList(), achievements = Lists.newArrayList();


    public PlayerWrapper(UUID uuid) {
        this.uuid = uuid;
    }

    public PlayerWrapper(Player player) {
        this(player.getUniqueId());
    }


    public void loadData(boolean async) {
        this.loadData(async, null);
    }

    public void loadData(boolean async, Consumer<PlayerWrapper> callback) {
        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> loadData(false, callback), SQL_EXECUTOR_SERVICE);
            return;
        }

        try {
            @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(
                    "SELECT * FROM `users` LEFT JOIN `ranks` ON `users`.`account_id` = `ranks`.`account_id` " +
                            "LEFT JOIN `toggles` ON `users`.`account_id` = `toggles`.`account_id` " +
                            "LEFT JOIN `mail` ON `users`.`account_id` = `mail`.`account_id` " +
                            "LEFT JOIN `guild_members` ON `users`.`account_id` = `guild_members`.`account_id` " +
                            "LEFT JOIN `guilds` ON `guild_members`.`guild_id` = `guilds`.`guild_id` " +
                            "LEFT JOIN `characters` ON `characters`.`character_id` = `users`.`selected_character_id` " +
                            "LEFT JOIN `attributes` ON `characters`.`character_id` = `attributes`.`character_id` " +
                            "LEFT JOIN `realm` ON `characters`.`character_id` = `realm`.`character_id` " +
                            "LEFT JOIN `statistics` ON `characters`.`character_id` = `statistics`.`character_id` " +
                            "WHERE `users`.`uuid` = ?;");
            statement.setString(1, uuid.toString());

            ResultSet result = statement.getResultSet();
            if (result.first()) {
                this.accountID = result.getInt("users.account_id");
                this.isPlaying = result.getBoolean("users.is_online");
                this.shardPlayingOn = result.getString("users.currentShard");
                this.characterID = result.getInt("characters.character_id");
                this.health = result.getInt("characters.health");
                this.level = result.getInt("characters.level");
                this.loadBanks(result);
                this.loadPlayerPendingInventory(result);
                this.loadPlayerPendingEquipment(result);
                this.loadMuleInventory(result);
                this.loadLocation(result);


                this.lastLogin = result.getLong("users.last_login");
                this.lastLogout = result.getLong("users.last_logout");
                this.lastShardTransfer = result.getLong("users.last_shard_transfer");

                this.playerStats = new PlayerStats(characterID);
                this.playerStats.extractData(result);

                this.ecash = result.getInt("users.ecash");
                this.lastFreeEcash = result.getLong("users.last_free_ecash");
                this.gems = result.getInt("characters.gems");
                this.experience = result.getInt("characters.experience");

                //String activeMount, activePet, activeTrail, activeMountSkin;
                this.activeMount = result.getString("characters.activeMount");
                this.activePet = result.getString("characters.activeMount");
                this.activeTrail = result.getString("characters.activeMount");
                this.activeMountSkin = result.getString("characters.activeMount");
                this.questData = result.getString("characters.questData");
                this.shopLevel = result.getInt("characters.shop_level");
                this.storedFoodLevel = result.getInt("characters.foodLevel");
                this.combatLogged = result.getBoolean("characters.combatLogged");



                if (callback != null)
                    callback.accept(this);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    private void loadLocation(ResultSet set) {
        String locString = set.getString("characters.location");
        //These coords are Cyren Bank. This is what it will use if there is any trouble parsing.
        int x = -371;
        int y = 83;
        int z = 340;

        try {
            String[] locArray = locString.split(",");
            x = Integer.parseInt(locArray[0]);
            y = Integer.parseInt(locArray[1]);
            z = Integer.parseInt(locArray[2]);
        } catch(Exception e) {
            e.printStackTrace();
        }

        this.storedLocation = new Location(Bukkit.getWorlds().get(0),x,y,z);
    }

    public void setPlaying(boolean playing) {
        if (playing == this.isPlaying) return;
        this.isPlaying = playing;

        try {
            @Cleanup PreparedStatement statement = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement(
                    "UPDATE users SET is_online = ?, currentShard = ?,  WHERE `users`.`uuid` = ?;");
            statement.setBoolean(1, this.isPlaying);
            statement.setString(2, this.isPlaying ? DungeonRealms.getShard().getShardID() : null);
            statement.setString(3, uuid.toString());

//            statement.

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveData(boolean async) {
        this.saveData(async, null);
    }

    public void saveData(boolean async, Consumer<PlayerWrapper> callback) {
        if (async && Bukkit.isPrimaryThread()) {
            CompletableFuture.runAsync(() -> saveData(false), SQL_EXECUTOR_SERVICE);
            return;
        }


        if (callback != null)
            callback.accept(this);
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

        //Auto set the inventory size based off level? min 9, max 54
        Inventory inv = ItemSerialization.fromString(serializedStorage, Math.max(9, Math.min(54, getBankLevel() * 9)));
        Storage storageTemp = new Storage(uuid, inv);
        loadCollectionBin(set, storageTemp);
        BankMechanics.storage.put(uuid, storageTemp);
    }

    @SneakyThrows
    private void loadCollectionBin(ResultSet set, Storage storage) {
        String stringInv = set.getString("characters.shop_storage");
        if (stringInv.length() > 1) {
            Inventory inv = ItemSerialization.fromString(stringInv);
            for (ItemStack item : inv.getContents())
                if (item != null && item.getType() == Material.AIR)
                    inv.addItem(item);

            storage.collection_bin = inv;
        }

    }

    @SneakyThrows
    private void loadPlayerPendingInventory(ResultSet set) {
        String playerInv = set.getString("characters.inventory_storage");
        if (playerInv != null && playerInv.length() > 0 && !playerInv.equalsIgnoreCase("null")) {
            pendingInventory = ItemSerialization.fromString(playerInv, 36);
        }
    }

    @SneakyThrows
    private void loadPlayerPendingEquipment(ResultSet set) {
        String playerEquipment = set.getString("characters.armour_storage");
        if (playerEquipment != null && playerEquipment.length() > 0 && !playerEquipment.equalsIgnoreCase("null")) {
            pendingArmor = ItemSerialization.fromString(playerEquipment, 9);
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
        Inventory muleInv = null;
        if (tier != null) {
            muleInv = Bukkit.createInventory(null, tier.getSize(), "Mule Storage");
            if (!invString.equalsIgnoreCase("") && !invString.equalsIgnoreCase("empty") && invString.length() > 4) {
                //Make sure the inventory is as big as we need
                muleInv = ItemSerialization.fromString(invString, tier.getSize());
            }
        }
        if (!invString.equalsIgnoreCase("") && !invString.equalsIgnoreCase("empty") && invString.length() > 4 && muleInv != null)
            MountUtils.inventories.put(uuid, muleInv);
    }

    private String getEquipmentString(Player player) {
        Inventory toSave = Bukkit.createInventory(null, 9);
        for (int index = 0; index < 5; index++) {
            ItemStack equipment = player.getEquipment().getArmorContents()[index];
            toSave.getContents()[index] = equipment;
        }

        return ItemSerialization.toString(toSave);
    }

    public void loadPlayerInventory(Player player) {
        if (pendingInventory == null) return;
        player.getInventory().setContents(pendingInventory.getContents());
        pendingInventory = null;
    }

    public void loadPlayerArmor(Player player) {
        if (pendingArmor == null) return;
        for (int index = 0; index < 5; index++) {
            //We are doing 5 for the new shield slot.
            ItemStack current = pendingArmor.getContents()[index];
            player.getEquipment().getArmorContents()[index] = current;
        }

        player.updateInventory();
    }


}
