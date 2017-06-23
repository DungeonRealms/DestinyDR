package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;

import org.bukkit.inventory.ItemStack;

/**
 * Represents all melee weapons, swords, axes, polearms.
 * @author Kneesnap
 */
public class ItemWeaponMelee extends ItemWeapon {

	public static final ItemType[] MELEE = new ItemType[] {ItemType.SWORD, ItemType.AXE, ItemType.POLEARM};

	public ItemWeaponMelee() {
		this(MELEE);
	}

	public ItemWeaponMelee(ItemType... type) {
		super(type);
	}

	public ItemWeaponMelee(ItemStack item) {
		super(item);
	}

	public static boolean isMelee(ItemStack item) {
		return ItemWeaponSword.isSword(item) || ItemWeaponAxe.isAxe(item) || ItemWeaponPolearm.isPolearm(item);
	}
}
