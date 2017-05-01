package net.dungeonrealms.game.world.loot;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;

/**
 * LootSpawner - Spawns loot chests around the map.
 * 
 * Redone on April 29th, 2017.
 * @author Kneesnap
 */
@Getter
public class LootSpawner {
	
	private Location location;
	private int tickDelay;
	private Inventory inventory = Bukkit.createInventory(null, 27, "Loot");
	private String lootTable;
	
	public LootSpawner(Location loc, int tickDelay, String loot) {
		this.location = loc;
		this.tickDelay = tickDelay;
		this.lootTable = loot;
		setContents();
	}
	
	public boolean isBroken() {
		return getLocation().getBlock().getType() != Material.CHEST && getInventory().getContents().length == 0;
	}
	
	/**
	 * Sets the loot in the chest based on tier.
	 */
	private void setContents() {
		getLocation().getBlock().setType(Material.CHEST);
		HashMap<ItemStack, Double> loot = LootManager.getLoot().get(getLootTable());
		
		int count = 0;
		for (ItemStack stack : loot.keySet()) {
			if (stack == null)
				continue;
			// Should we spawn this item?
			if (loot.get(stack) < new Random().nextInt(1000))
				continue;
			
			ItemGeneric item = (ItemGeneric)PersistentItem.constructItem(stack);
			item.removeEpoch();
			addToRandomSlot(item.generateItem());
			count++;
		}
		
		// No items generated :S better try again.
		if (count == 0)
			setContents();
	}
	
	private void addToRandomSlot(ItemStack item) {
		if (getInventory().firstEmpty() == -1)
			return;
		
		int slot = Utils.randInt(0, getInventory().getContents().length - 1);
		
		ItemStack s = getInventory().getItem(slot);
		// This slot is occupied.
		if (s != null && s.getType() != Material.AIR) {
			addToRandomSlot(item);
			return;
		}
		
		getInventory().setItem(slot, item);
	}

	/**
	 * Attempt to break this loot spawner.
	 */
	public void attemptBreak(Player player) {
		if (getInventory().getContents().length > 0)
			for (ItemStack stack : getInventory().getContents())
				if (stack != null && stack.getType() != Material.AIR)
					return;
		
		World world = getLocation().getWorld();
		
		GamePlayer gamePlayer = GameAPI.getGamePlayer(player);
		gamePlayer.getPlayerStatistics().setLootChestsOpened(gamePlayer.getPlayerStatistics().getLootChestsOpened() + 1);
		
		for (int i = 0; i < 6; i++)
			for (double yOffset = 0.2; yOffset <= 0.5; yOffset += 0.15)
        		world.playEffect(getLocation().clone().add(i, yOffset, i), Effect.TILE_BREAK, 25, 12);
        
		world.playSound(getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 0.5f, 1.2f);
		getLocation().getBlock().getDrops().clear();
		getLocation().getBlock().setType(Material.AIR);
		
		Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), this::setContents,
			(long) (getTickDelay() + (getTickDelay() * LootManager.getDelayMultiplier())));
	}
	
	/**
	 * Draws enchantment particles around it.
	 */
	public void showParticles() {
		getLocation().getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, getLocation(), 20, .2D, .2D, .2D);
	}
}
