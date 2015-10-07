package net.dungeonrealms.banks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

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
     * @param inventory
     */
    public Storage(UUID uuid, Inventory inventory) {
        ownerUUID = uuid;
        this.inv = inventory;
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
