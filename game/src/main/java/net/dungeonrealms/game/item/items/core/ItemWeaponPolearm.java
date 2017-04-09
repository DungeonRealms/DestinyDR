package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;

import org.bukkit.inventory.ItemStack;

/**
 * A basic polearm item.
 * 
 * Created April 2nd, 2017.
 * @author Kneesnap
 */
public class ItemWeaponPolearm extends ItemWeaponMelee {

	public ItemWeaponPolearm() {
		super(ItemType.POLEARM);
	}
	
	public ItemWeaponPolearm(ItemStack item) {
		super(item);
	}
	
	public static boolean isPolearm(ItemStack item) {
		return isType(item, ItemType.POLEARM);
	}
}
