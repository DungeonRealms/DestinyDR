package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A base for ranged weapons such as staffes and bows.
 * @author Kneesnap
 */
public abstract class ItemWeaponRanged extends ItemWeapon {
	
	public ItemWeaponRanged(ItemType type) {
		super(type);
	}
	
	public ItemWeaponRanged(ItemStack item) {
		super(item);
	}
	
	public static boolean isRangedWeapon(ItemStack item) {
		return ItemWeaponStaff.isStaff(item) || ItemWeaponBow.isBow(item);
	}

	/**
	 * Returns the delay in milliseconds between shooting of projectiles.
	 */
	public abstract int getShootDelay();
	
	/**
	 * Get the sound played when a projectile is launched.
	 */
	public abstract Sound getShootSound();
	
	/**
	 * Spawn this projectile in the world.
	 */
	public abstract void fireProjectile(Player player, boolean takeDurability);
}
