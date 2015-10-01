package net.dungeonrealms.banks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * Created by Chase on Sep 25, 2015
 */
public class Storage {

    public UUID ownerUUID;
    public Inventory inv;

    public Storage(UUID owner) {
        ownerUUID = owner;
        inv = getNewStorage();
    }

    /**
     * @param uuid
     * @param contents
     */
    public Storage(UUID uuid, List<ItemStack> contents) {
        ownerUUID = uuid;
        Inventory inv = Bukkit.createInventory(null, getStorageSize(Bukkit.getPlayer(uuid)), "Storage");
        for (ItemStack content : contents) {
            inv.addItem(content);
        }
        this.inv = inv;
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
        if (p.getName().equalsIgnoreCase("Xwaffle"))
            return 18;
        return 9;
    }
}
