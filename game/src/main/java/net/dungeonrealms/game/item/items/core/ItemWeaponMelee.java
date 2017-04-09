package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;

import org.bukkit.inventory.ItemStack;

/**
 * Represents all melee weapons, swords, axes, polearms.
 * @author Kneesnap
 */
public class ItemWeaponMelee extends ItemWeapon {
	
	public ItemWeaponMelee() {
		super(ItemType.MELEE, ItemType.POLEARM);
	}
	
	public ItemWeaponMelee(ItemType type) {
		super(type);
	}
	
	public ItemWeaponMelee(ItemStack item) {
		super(item);
	}
	
	public static boolean isMelee(ItemStack item) {
		return isType(item, ItemType.MELEE) || ItemWeaponPolearm.isPolearm(item);
	}
}
