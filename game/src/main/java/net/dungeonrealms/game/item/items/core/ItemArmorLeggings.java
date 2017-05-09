package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;

import org.bukkit.inventory.ItemStack;

public class ItemArmorLeggings extends ItemArmor {
	
	public ItemArmorLeggings() {
		super(ItemType.LEGGINGS);
	}
	
	public ItemArmorLeggings(ItemStack item) {
		super(item);
	}
	
	public static boolean isLeggings(ItemStack i) {
		return isType(i, ItemType.LEGGINGS);
	}
}