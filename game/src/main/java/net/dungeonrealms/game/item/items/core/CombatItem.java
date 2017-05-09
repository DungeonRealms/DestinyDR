package net.dungeonrealms.game.item.items.core;

import java.util.Random;

import net.dungeonrealms.game.item.ItemType;

import org.bukkit.inventory.ItemStack;

/**
 * Represents any item used in combat.
 * @author Kneesnap
 */
public abstract class CombatItem extends ItemGear {
	
	public CombatItem() {
		this(new Random().nextBoolean() ? ItemArmor.ARMOR : ItemWeapon.WEAPONS);
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
