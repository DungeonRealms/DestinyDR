package net.dungeonrealms.game.item.items.functional;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.world.item.Item.ItemTier;

public class ItemEnchantArmor extends ItemEnchantScroll {
	
	public ItemEnchantArmor(int i) {
		this(ItemTier.getByTier(i));
	}
	
	public ItemEnchantArmor(ItemTier tier) {
		super(tier, ItemType.ENCHANT_ARMOR, "Armor");
	}
	
	public ItemEnchantArmor() {
		this(ItemTier.TIER_1);
	}
	
	public ItemEnchantArmor(ItemStack stack) {
		super(stack);
	}

	@Override
	protected String[] getLore() {
		return new String[] {
				ChatColor.RED + "+5% HP",
				ChatColor.RED + "+5% HP REGEN",
				ChatColor.ITALIC + "    - OR -",
				ChatColor.RED + "+1% ENERGY REGEN",
				ChatColor.ITALIC + "Armor will VANISH if enchant above +3 FAILS."};
	}

	@Override
	protected boolean isApplicable(ItemStack item) {
		return ItemArmor.isArmor(item);
	}
	
	public static boolean isEnchant(ItemStack item) {
		return isType(item, ItemType.ENCHANT_ARMOR);
	}
}
