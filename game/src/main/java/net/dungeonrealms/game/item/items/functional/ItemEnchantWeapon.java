package net.dungeonrealms.game.item.items.functional;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.world.item.Item.ItemTier;

public class ItemEnchantWeapon extends ItemEnchantScroll {
	
	public ItemEnchantWeapon() {
		this(ItemTier.TIER_1);
	}
	
	public ItemEnchantWeapon(int i) {
		this(ItemTier.getByTier(i));
	}
	
	public ItemEnchantWeapon(ItemTier tier) {
		super(tier, ItemType.ENCHANT_WEAPON, "Weapon");
	}
	
	public ItemEnchantWeapon(ItemStack stack) {
		super(stack);
	}
	
	@Override
	protected String[] getLore() {
		return new String[] {
				ChatColor.RED + "+5% DMG",
				ChatColor.ITALIC + "Weapon will VANISH if enchant above +3 FAILS."};
	}

	@Override
	protected boolean isApplicable(ItemStack item) {
		return ItemWeapon.isWeapon(item);
	}
	
	public static boolean isEnchant(ItemStack item) {
		return isType(item, ItemType.ENCHANT_WEAPON);
	}
}
