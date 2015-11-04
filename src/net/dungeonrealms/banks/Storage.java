package net.dungeonrealms.banks;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;

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
        this.inv = getNewStorage();
        for(org.bukkit.inventory.ItemStack stack : inventory.getContents()){
        	if(stack != null && stack.getType() != org.bukkit.Material.AIR)
        	if(inv.firstEmpty() >= 0)
        		inv.addItem(stack);
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
	}

	/**
	 * 
	 */
	public void upgrade() {
		// TODO Auto-generated method stub
		
	}
}
