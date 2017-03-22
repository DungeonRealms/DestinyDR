package net.dungeonrealms.game.player.banks;

import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.mastery.ItemSerialization;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

        for (int i = 0; i < this.inv.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR)
                inv.setItem(i, item);
        }
//        for (org.bukkit.inventory.ItemStack stack : inventory.getContents()) {
//            if (stack != null && stack.getType() != org.bukkit.Material.AIR)
//                if (inv.firstEmpty() >= 0)
//                    inv.addItem(stack);
//        }
        String stringInv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_COLLECTION_BIN, ownerUUID);
        //Without this VV Players can /closeshop and dupe their items.
        DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, "", true, true);
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
            for (ItemStack item : inv.getContents())
                if (item != null && item.getType() == Material.AIR)
                    inv.addItem(item);
            
            Player p = Bukkit.getPlayer(ownerUUID);
            if (p != null)
                p.sendMessage(ChatColor.RED + "You have items in your collection bin!");
            this.collection_bin = inv;
            DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, "", true, true);
        }
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
