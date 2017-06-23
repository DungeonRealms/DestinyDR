package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.world.item.Item.WeaponAttributeType;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A basic weapon class.
 * @author Kneesnap
 */
public class ItemWeapon extends CombatItem {
	
	public final static ItemType[] WEAPONS = new ItemType[] {ItemType.BOW, ItemType.STAFF, ItemType.SWORD, ItemType.AXE, ItemType.POLEARM};
	
	public ItemWeapon() {
		this(WEAPONS);
	}
	
	public ItemWeapon(ItemType... type) {
		super(type);
	}
	
	public ItemWeapon(ItemStack item) {
		super(item);
	}

	@Override
	protected void applyEnchantStats() {
		getAttributes().multiplyStat(WeaponAttributeType.DAMAGE, 1.05);
	}

	@Override
	protected double getBaseRepairCost() {
		return getAttributes().getAttribute(WeaponAttributeType.DAMAGE).getMiddle() / (getItemType() == ItemType.POLEARM ? 6.5 : 10);
	}

	@Override
	protected void onItemBreak(Player player) {
		//Don't need to do anything.
	}
	
	public static boolean isWeapon(ItemStack item) {
		return ItemWeaponMelee.isMelee(item) || ItemWeaponRanged.isRangedWeapon(item);
	}

}
