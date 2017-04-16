package net.dungeonrealms.game.item.items.core;

import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;

/**
 * ItemWeaponAxe
 */
public class ItemWeaponAxe extends ItemWeaponMelee {

	public ItemWeaponAxe() {
		super(ItemType.AXE);
	}
	
	public ItemWeaponAxe(ItemStack item) {
		super(item);
	}
	
	public static boolean isAxe(ItemStack item) {
		return isType(item, ItemType.AXE);
	}
}
