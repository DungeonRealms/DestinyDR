package net.dungeonrealms.spawning;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mechanics.LootManager;

/**
 * Created by Chase on Oct 9, 2015
 */
public class LootSpawner {

	public int tier = 1;
	public Location location;
	public Block block;
	public Inventory inv;
	public boolean broken;

	public LootSpawner(Location loc, int tier, Block b) {
		this.location = loc;
		this.tier = tier;
		block = b;
		inv = Bukkit.createInventory(null, 27, "Loot");
		setContents();
		broken = false;
	}

	/**
	 * Sets the loot in the chest based on tier.
	 */
	private void setContents() {
		ArrayList<ItemStack> loot = LootManager.getLoot(tier);
		for(int i = 0; i < loot.size(); i++)
			inv.addItem(loot.get(i));
	}

	/**
	 * Checking if the inventory is empty, then break the chest.
	 */
	public void update() {
		if (inv.getContents().length > 0)
			for (ItemStack stack : inv.getContents()) {
				if (stack != null)
					if (stack.getType() != (Material.AIR))
						return;
			}
		block.getDrops().clear();
		block.setType(Material.AIR);
		broken = true;
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
			setContents();
			block.setType(Material.CHEST);
		} , 20 * 120l);
	}

}
