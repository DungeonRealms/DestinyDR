package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;

import org.bukkit.inventory.ItemStack;

/**
 * Represents any item used in combat.
 * @author Kneesnap
 */
public abstract class CombatItem extends ItemGear {
	
	public CombatItem() {
		super(ItemType.ARMOR, ItemType.BOW, ItemType.POLEARM, ItemType.STAFF, ItemType.MELEE);
	}
	
	public CombatItem(ItemType... type) {
		super(type);
	}
	
	public CombatItem(ItemStack item) {
		super(item);
	}
	
	public static boolean isCombatItem(ItemStack item) {
		return ItemArmor.isArmor(item) || ItemWeapon.isWeapon(item);
	}
}
