package net.dungeonrealms.game.world.loot;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.game.mastery.Utils;

import net.dungeonrealms.game.mechanic.ParticleAPI;
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
	private String table;
	private boolean broken; // Cached so the chest particles don't throw an async error.
	
	public LootSpawner(Location loc, int tickDelay, String table) {
		this.location = loc;
		this.tickDelay = tickDelay;
		this.table = table;
		setContents();
	}
	
	/**
	 * Is this lootchest broken? (Async Safe!)
	 */
	public boolean isBroken() {
		if (Bukkit.isPrimaryThread())
			broken = (!getLocation().getChunk().isLoaded() || getLocation().getBlock().getType() != Material.CHEST) && getInventory().getContents().length == 0;
		return this.broken;
	}
	
	/**
	 * Sets the loot in the chest based on tier.
	 */
	public void setContents() {
		getLocation().getBlock().setType(Material.CHEST);
		isBroken();
		getLootTable().generateLoot().forEach(this::addToRandomSlot); // Add the items to the chest.
	}
	
	/**
	 * Get the loot table for this chest.
	 */
	public LootTable getLootTable() {
		return LootManager.getLoot().get(this.table);
	}
	
	/**
	 * Gets the delay it will take to respawn this loot chest.
	 */
	public int getTickDelay() {
		return this.tickDelay + 1200;
	}
	
	/**
	 * Add an item to a random slot in this inventory.
	 */
	private void addToRandomSlot(ItemStack item) {
		if (getInventory().firstEmpty() == -1) // There's no place to add this item.
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
		
		PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
		wrapper.getPlayerGameStats().addStat(StatColumn.LOOT_OPENED);

		world.playEffect(getLocation().clone().add(0.5, 0, .5), Effect.STEP_SOUND, Material.CHEST.getId());

		world.playSound(getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 0.5f, 1.2f);
		getLocation().getBlock().getDrops().clear();
		getLocation().getBlock().setType(Material.AIR);
		
		Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), this::setContents,
			(long) (getTickDelay() + (getTickDelay() * LootManager.getDelayMultiplier())));
		isBroken();
	}
	
	/**
	 * Draws enchantment particles around it.
	 */
	public void showParticles() {
		Location half = getLocation().clone().add(0.5, 0, 0.5);
		ParticleAPI.sendParticleToLocationAsync(ParticleAPI.ParticleEffect.ENCHANTMENT_TABLE, half, .2F, .2F, .2F, .01F, 15);
//		getLocation().getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, getLocation(), 20, .2D, .2D, .2D);
	}
	
	@Override
	public String toString() {
		Location l = getLocation();
		return l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "=" + this.table + "@" + this.tickDelay + "#";
	}
}