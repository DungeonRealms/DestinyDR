package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;

import org.bukkit.inventory.ItemStack;

public class ItemArmorBoots extends ItemArmor {
	
	public ItemArmorBoots() {
		super(ItemType.BOOTS);
	}
	
	public ItemArmorBoots(ItemStack item) {
		super(item);
	}
	
	public static boolean isBoots(ItemStack i) {
		return isType(i, ItemType.BOOTS);
	}
}