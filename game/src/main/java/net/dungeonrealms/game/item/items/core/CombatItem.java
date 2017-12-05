package net.dungeonrealms.game.item.items.core;

import java.util.concurrent.ThreadLocalRandom;

import net.dungeonrealms.game.item.ItemType;

import net.dungeonrealms.game.item.items.ItemUtilityWeapon;
import org.bukkit.inventory.ItemStack;

/**
 * Represents any item used in combat.
 * @author Kneesnap
 */
public abstract class CombatItem extends ItemGear {
	
	public CombatItem() {
		this(ThreadLocalRandom.current().nextBoolean() ? ItemArmor.ARMOR : ThreadLocalRandom.current().nextBoolean() ? ItemUtilityWeapon.UTILITY_WEAPONS : ItemWeapon.WEAPONS);
	}
	
	public CombatItem(ItemType... type) {
		super(type);
	}
	
	public CombatItem(ItemStack item) {
		super(item);
	}
	
	public static boolean isCombatItem(ItemStack item) {
		return ItemArmor.isArmor(item) || ItemWeapon.isWeapon(item) || ItemUtilityWeapon.isUtilityWeapon(item);
	}
}
