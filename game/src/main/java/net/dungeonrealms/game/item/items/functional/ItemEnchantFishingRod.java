package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemFishingPole;

import org.bukkit.inventory.ItemStack;

public class ItemEnchantFishingRod extends ItemEnchantProfession {
	
	public ItemEnchantFishingRod() {
		this((ItemFishingPole) null);
	}
	
	public ItemEnchantFishingRod(ItemStack stack) {
		super(stack);
	}
	
	public ItemEnchantFishingRod(ItemFishingPole pole) {
		super(ItemType.ENCHANT_FISHING_ROD, "Fishing Rod", pole);
	}

	@Override
	protected ItemType applyTo() {
		return ItemType.FISHING_POLE;
	}
	
	public static boolean isEnchant(ItemStack stack) {
		return isType(stack, ItemType.ENCHANT_FISHING_ROD);
	}
}
