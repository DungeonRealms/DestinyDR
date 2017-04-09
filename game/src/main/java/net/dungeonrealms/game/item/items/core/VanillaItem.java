package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;

import org.bukkit.inventory.ItemStack;

/**
 * A generic item that can be constructed with any bukkit itemstack.
 * @author Kneesnap
 */
public class VanillaItem extends ItemGeneric {

	public VanillaItem(ItemStack item) {
		super(item);
	}

	@Override
	protected ItemStack getStack() {
		return getItem();
	}
	
	@Override
	public ItemType getItemType() {
		return null;
	}
}
