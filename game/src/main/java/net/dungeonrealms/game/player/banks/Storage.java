package net.dungeonrealms.game.player.banks;

import com.google.common.collect.Lists;
import lombok.Cleanup;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.ItemSerialization;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Chase on Sep 25, 2015
 */
public class Storage {

    public UUID ownerUUID;
    @Getter
    private int characterID;
    public Inventory inv;
    public Inventory collection_bin = null;

    public Storage(UUID owner, int accountID) {
        ownerUUID = owner;
        inv = getNewStorage();
        this.characterID = accountID;
    }

    /**
     * @param uuid
     * @param inventory
     */
    public Storage(UUID uuid, Inventory inventory, int characterID) {
        ownerUUID = uuid;
        this.characterID = characterID;
        this.inv = getNewStorage();

        for (int i = 0; i < this.inv.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                inv.setItem(i, item);
            }
        }
        //Loading auto on create? Why?
//        update();
    }

    public void clearCollectionBin() {
        if (collection_bin == null)
            return;

        if (this.characterID != 0)
            SQLDatabaseAPI.getInstance().getSqlQueries().add("UPDATE characters SET collection_storage = null WHERE character_id = '" + this.characterID + "'");

        //        DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, "", true, true);
        //VV Clears the current inventory so any viewers don't get to take it.
        collection_bin.clear();
        collection_bin = null;
    }

    /**
     * @return
     */
    private Inventory getNewStorage() {
//        Player p = Bukkit.getPlayer(ownerUUID);
        int size = getStorageSize();
        return Bukkit.createInventory(null, size, "Storage Chest");
    }

    /**
     * @param p
     * @return
     */
    private int getStorageSize() {
        int lvl = (Integer) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_LEVEL, ownerUUID);
        return 9 * lvl;
    }

    private PlayerWrapper getPlayerWrapper() {
        return PlayerWrapper.getPlayerWrapper(this.ownerUUID);
    }

    /**
     * Used to update inventory size when upgraded.
     */
    public void update() {
        Inventory inventory = getNewStorage();
        if (inv != null)
            inventory.setContents(inv.getContents());
        this.inv = inventory;

        CompletableFuture.runAsync(() -> {
            try {
                @Cleanup PreparedStatement state = SQLDatabaseAPI.getInstance().getDatabase().getConnection().prepareStatement("SELECT collection_storage FROM characters WHERE character_id = '" + this.characterID + "';");
                ResultSet rs = state.executeQuery();
                if (rs.first()) {
                    String newBin = rs.getString("users.collection_storage");
                    if (newBin != null && newBin.length() > 1) {
                        //We have some collection bin data..
                        Inventory inv = ItemSerialization.fromString(newBin);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            Player p = Bukkit.getPlayer(ownerUUID);
                            if (p != null)
                                p.sendMessage(ChatColor.RED + "You have items in your collection bin!");

                            //Clear old bin?
                            if (this.collection_bin != null) {
                                this.collection_bin.clear();
                                //Close thier views..
                                Lists.newArrayList(this.collection_bin.getViewers()).forEach(HumanEntity::closeInventory);
                            }

                            this.collection_bin = inv;
                            SQLDatabaseAPI.getInstance().getSqlQueries().add("UPDATE characters SET collection_storage WHERE character_id = '" + this.characterID + "';");
                        });
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }, SQLDatabaseAPI.getInstance().getSERVER_EXECUTOR_SERVICE());
        //Pulling collection bin from cached doc.
//        String stringInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_COLLECTION_BIN, ownerUUID);
//        if (stringInv.length() > 1) {
//            Inventory inv = ItemSerialization.fromString(stringInv);
//            for (ItemStack item : inv.getContents())
//                if (item != null && item.getType() == Material.AIR)
//                    inv.addItem(item);

//        Player p = Bukkit.getPlayer(ownerUUID);
//        if (p != null)
//            p.sendMessage(ChatColor.RED + "You have items in your collection bin!");
//        this.collection_bin = inv;
        //Clears the collectionbin from the database...
//        DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, "", true, true);
    }


    public boolean hasSpace() {
        for (ItemStack stack : inv.getContents())
            if (stack == null || stack.getType() == Material.AIR)
                return true;
        return false;
    }

    public void upgrade() {
    }

    public void openBank(Player player) {
        if (collection_bin != null) {
            player.sendMessage(ChatColor.RED + "You have item(s) waiting in your collection bin.");
            player.sendMessage(ChatColor.GRAY + "Access your bank chest to claim them.");
            return;
        }

        player.openInventory(inv);
    }
}
