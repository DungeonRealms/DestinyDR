package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;

import org.bukkit.inventory.ItemStack;

public class ItemArmorHelmet extends ItemArmor {
		
	public ItemArmorHelmet() {
		super(ItemType.HELMET);
	}
	
	public ItemArmorHelmet(ItemStack item) {
		super(item);
	}
	
	public static boolean isHelmet(ItemStack i) {
		return isType(i, ItemType.HELMET);
	}
}