package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;

import org.bukkit.inventory.ItemStack;

/**
 * ItemWeaponSword
 */
public class ItemWeaponSword extends ItemWeaponMelee{

	public ItemWeaponSword() {
		super(ItemType.SWORD);
	}
	
	public ItemWeaponSword(ItemStack item) {
		super(item);
	}
	
	public static boolean isSword(ItemStack item) {
		return isType(item, ItemType.SWORD);
	}
}
