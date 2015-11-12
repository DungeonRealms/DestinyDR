package net.dungeonrealms.loot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.loot.types.LootType;

/**
 * Created by Chase on Oct 9, 2015
 */
public class LootSpawner {

	public long delay = 100;
	public Location location;
	public Block block;
	public Inventory inv;
	public boolean broken;
	public LootType lootType;

	public LootSpawner(Block chest, long delay, LootType lootType) {
		this.location = chest.getLocation();
		this.delay = delay;
		this.lootType = lootType;
		block = chest;
		inv = Bukkit.createInventory(null, 27, "Loot");
		setContents();
		broken = false;
	}

	/**
	 * Sets the loot in the chest based on tier.
	 */
	private void setContents() {
		HashMap<ItemStack, Double> loot = lootType.getLoot();
		int count = 0;
		for(Map.Entry<ItemStack, Double> booty : loot.entrySet()){
			ItemStack stack = booty.getKey();
			double spawn_chance = loot.get(stack);
			double do_i_spawn = new Random().nextInt(1000);
			if (spawn_chance < 1) {
				spawn_chance = 1;
			}
			if (spawn_chance >= do_i_spawn) {
				if(stack.getType() == Material.IRON_SWORD){
					int tier = CraftItemStack.asNMSCopy(stack).getTag().getInt("itemTier");
					stack = LootManager.generateRandomTierItem(tier);
				}
				count++;
				inv.addItem(stack);
			}
		}
		
		if(count == 0){
			setContents();
		}
		
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
		}, (long) (delay  * LootManager.getDelayMultiplier()) );
	}

}
