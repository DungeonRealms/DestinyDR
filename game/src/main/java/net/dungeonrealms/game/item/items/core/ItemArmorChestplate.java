package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;

import org.bukkit.inventory.ItemStack;

public class ItemArmorChestplate extends ItemArmor {
	
	public ItemArmorChestplate() {
		super(ItemType.CHESTPLATE);
	}
	
	public ItemArmorChestplate(ItemStack item) {
		super(item);
	}
	
	public static boolean isChestplate(ItemStack i) {
		return isType(i, ItemType.CHESTPLATE);
	}
}
