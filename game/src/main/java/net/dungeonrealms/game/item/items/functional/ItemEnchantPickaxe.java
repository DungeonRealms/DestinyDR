package net.dungeonrealms.game.item.items.functional;

import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemPickaxe;
import net.dungeonrealms.game.world.item.Item.PickaxeAttributeType;
import net.dungeonrealms.game.world.item.Item.ProfessionAttribute;

public class ItemEnchantPickaxe extends ItemEnchantProfession {
	
	public ItemEnchantPickaxe(PickaxeAttributeType type) {
		this();
		add(type);
	}
	
	public ItemEnchantPickaxe() {
		super(ItemType.ENCHANT_PICKAXE, "Pickaxe");
	}
	
	public ItemEnchantPickaxe(ItemStack stack) {
		super(stack);
	}

	@Override
	protected boolean isApplicable(ItemStack item) {
		return ItemPickaxe.isPickaxe(item);
	}

	@Override
	protected String[] getLore() {
		return new String[] { "Imbues a pickaxe with special attributes." };
	}

	@Override
	protected ProfessionAttribute[] getValues() {
		return PickaxeAttributeType.values();
	}
	
	public static boolean isEnchant(ItemStack item) {
		return isType(item, ItemType.ENCHANT_PICKAXE);
	}
}