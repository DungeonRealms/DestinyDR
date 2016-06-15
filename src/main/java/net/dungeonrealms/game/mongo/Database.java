package net.dungeonrealms.game.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.guild.db.GuildDatabase;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.mastery.AsyncUtils;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.entities.utils.MountUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Nick on 8/29/2015.
 */
public class Database {

    private static Database instance = null;

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public static com.mongodb.MongoClient mongoClient = null;
    public static MongoClientURI mongoClientURI = null;
    public static com.mongodb.client.MongoDatabase database = null;
    public static com.mongodb.client.MongoCollection<Document> collection = null;
    public static com.mongodb.client.MongoCollection<Document> ranks = null;
    public static com.mongodb.client.MongoCollection<Document> guilds = null;

    public void startInitialization() {
        Utils.log.info("DungeonRealms Starting [MONGODB] Connection...");
        mongoClientURI = new MongoClientURI("mongodb://104.236.116.27:27017/dungeonrealms");
        mongoClient = new MongoClient(mongoClientURI);
        //mongoClient = MongoClients.create("mongodb://104.236.116.27:27017/dungeonrealms");
        database = mongoClient.getDatabase("dungeonrealms");
        collection = database.getCollection("player_data");
        guilds = database.getCollection("guilds");
        ranks = database.getCollection("ranks");

        GuildDatabase.setGuilds(guilds);
        Utils.log.info("DungeonRealms [MONGODB] has connected successfully!");
    }

    public void backupDatabase() {
        if (Bukkit.getOnlinePlayers().size() == 0) return;
        AsyncUtils.pool.submit(() -> {
            DungeonRealms.getInstance().getLogger().info("Beginning Mongo Database Backup");
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!DatabaseAPI.getInstance().PLAYERS.containsKey(player.getUniqueId())) {
                    return;
                }
                UUID uuid = player.getUniqueId();
                if (BankMechanics.storage.containsKey(uuid)) {
                    Inventory inv = BankMechanics.getInstance().getStorage(uuid).inv;
                    if (inv != null) {
                        String serializedInv = ItemSerialization.toString(inv);
                        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY_STORAGE, serializedInv, false);
                    }
                    inv = BankMechanics.getInstance().getStorage(uuid).collection_bin;
                    if (inv != null) {
                        String serializedInv = ItemSerialization.toString(inv);
                        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, serializedInv, false);
                    }
                }
                Inventory inv = player.getInventory();
                ArrayList<String> armor = new ArrayList<>();
                for (ItemStack itemStack : player.getInventory().getArmorContents()) {
                    if (itemStack == null || itemStack.getType() == Material.AIR) {
                        armor.add("null");
                    } else {
                        armor.add(ItemSerialization.itemStackToBase64(itemStack));
                    }
                }
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ARMOR, armor, false);
                if (MountUtils.inventories.containsKey(uuid)) {
                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY_MULE, ItemSerialization.toString(MountUtils.inventories.get(uuid)), false);
                }
                String locationAsString = "-367,86,390,0,0"; // Cyrennica
                if (player.getWorld().equals(Bukkit.getWorlds().get(0))) {
                    locationAsString = player.getLocation().getX() + "," + (player.getLocation().getY() + 0.5) + ","
                            + player.getLocation().getZ() + "," + player.getLocation().getYaw() + ","
                            + player.getLocation().getPitch();
                }
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.CURRENT_LOCATION, locationAsString, false);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.CURRENT_FOOD, player.getFoodLevel(), false);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.HEALTH, HealthHandler.getInstance().getPlayerHPLive(player), false);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT, KarmaHandler.getInstance().getPlayerRawAlignment(player), false);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.ALIGNMENT_TIME, KarmaHandler.getInstance().getAlignmentTime(player), false);
                String inventory = ItemSerialization.toString(inv);
                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.INVENTORY, inventory, false);
                if (API.GAMEPLAYERS.size() > 0) {
                    API.GAMEPLAYERS.stream().filter(gPlayer -> gPlayer.getPlayer().getName().equalsIgnoreCase(player.getName())).forEach(gPlayer -> gPlayer.getStats().updateDatabase(false));
                }
            }
            DungeonRealms.getInstance().getLogger().info("Completed Mongo Database Backup");
        });
    }

}
