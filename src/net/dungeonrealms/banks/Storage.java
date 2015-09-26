/**
 * 
 */
package net.dungeonrealms.banks;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
		for(int i = 0; i<contents.size();i++){
		inv.addItem(contents.get(i));
		contents.remove(i);
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
		return 9;
	}
}
