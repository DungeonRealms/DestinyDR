package net.dungeonrealms.game.player.banks;

import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Chase on Sep 25, 2015
 */
public class Storage {

    public UUID ownerUUID;
    public Inventory inv;
    public Inventory collection_bin = null;

    public Storage(UUID owner) {
        ownerUUID = owner;
        inv = getNewStorage();
    }

    /**
     * @param uuid
     * @param inventory
     */
    public Storage(UUID uuid, Inventory inventory) {
        ownerUUID = uuid;
        this.inv = getNewStorage();
        for (org.bukkit.inventory.ItemStack stack : inventory.getContents()) {
            if (stack != null && stack.getType() != org.bukkit.Material.AIR)
                if (inv.firstEmpty() >= 0)
                    inv.addItem(stack);
        }
        String stringInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_COLLECTION_BIN, ownerUUID);
        if (stringInv.length() > 1) {
            Inventory inv = ItemSerialization.fromString(stringInv);
            for (ItemStack item : inv.getContents()) {
                if (item != null && item.getType() == Material.AIR) {
                    inv.addItem(item);
                }
            }
            this.collection_bin = inv;
        }

    }

    /**
     * @return
     */
    private Inventory getNewStorage() {
        Player p = Bukkit.getPlayer(ownerUUID);
        int size = getStorageSize(p);
        return Bukkit.createInventory(p, size, "Storage Chest");
    }

    /**
     * @param p
     * @return
     */
    private int getStorageSize(Player p) {
        int lvl = (Integer) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_LEVEL, p.getUniqueId());
        return 9 * lvl;
    }

    /**
     * Used to update inventory size when upgraded.
     */
    public void update() {
        Inventory inventory = getNewStorage();
        inventory.setContents(inv.getContents());
        this.inv = inventory;
        String stringInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_COLLECTION_BIN, ownerUUID);
        if (stringInv.length() > 1) {
            Inventory inv = ItemSerialization.fromString(stringInv);
            for (ItemStack item : inv.getContents()) {
                if (item != null && item.getType() == Material.AIR) {
                    inv.addItem(item);
                }
            }
            this.collection_bin = inv;
        }
    }

    public boolean hasSpace() {
        for (ItemStack stack : inv.getContents()) {
            if (stack == null || stack.getType() == Material.AIR)
                return true;
        }
        return false;
    }

    public void upgrade() {
    }
}
