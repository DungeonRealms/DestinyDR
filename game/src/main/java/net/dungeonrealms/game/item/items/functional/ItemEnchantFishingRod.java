package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemFishingPole;
import net.dungeonrealms.game.world.item.Item.FishingAttributeType;
import net.dungeonrealms.game.world.item.Item.ProfessionAttribute;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class ItemEnchantFishingRod extends ItemEnchantProfession {

	public ItemEnchantFishingRod(FishingAttributeType type) {
		this();
		add(type);
	}
	
	public ItemEnchantFishingRod(ItemStack stack) {
		super(stack);
	}
	
	public ItemEnchantFishingRod(ItemFishingPole pole) {
		super(ItemType.ENCHANT_FISHING_ROD, "Fishing Rod", pole);
	}
	
	public ItemEnchantFishingRod() {
		this((ItemFishingPole)null);
	}

	@Override
	protected ProfessionAttribute[] getValues() {
		return FishingAttributeType.values();
	}

	@Override
	protected boolean isApplicable(ItemStack item) {
		return ItemFishingPole.isFishingPole(item);
	}

	@Override
	protected String[] getLore() {
		return new String[] { ChatColor.GRAY + "Imbues a fishing rod with special attributes." };
	}
	
	public static boolean isEnchant(ItemStack stack) {
		return isType(stack, ItemType.ENCHANT_FISHING_ROD);
	}
}
