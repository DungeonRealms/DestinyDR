package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A basic class that represents all custom DR armor.
 * @author Kneesnap
 */
public class ItemArmor extends CombatItem {

	public final static ItemType[] ARMOR = new ItemType[] {ItemType.BOOTS, ItemType.LEGGINGS, ItemType.CHESTPLATE, ItemType.HELMET};
	
	public ItemArmor() {
		this(ARMOR);
	}
	
	public ItemArmor(ItemType... type) {
		super(type);
	}
	
	public ItemArmor(ItemStack item) {
		super(item);
	}
	
	/**
	 * Generates an entire armor set with the parameters of this item.
	 */
	public ItemStack[] generateArmorSet() {
		ItemStack[] ret = new ItemStack[ARMOR.length];
		for (int i = 0; i < ARMOR.length; i++)
			ret[i] = new ItemArmor(ARMOR[i]).setTier(getTier()).setRarity(getRarity()).generateItem();
		return ret;
	}

	@Override
	protected void applyEnchantStats() {
		getAttributes().multiplyStat(ArmorAttributeType.HEALTH_POINTS, 1.05);
		getAttributes().multiplyStat(ArmorAttributeType.HEALTH_REGEN, 1.05);
		getAttributes().addStat(ArmorAttributeType.ENERGY_REGEN, 1);
	}

	@Override
	protected double getBaseRepairCost() {
		if (getAttributes().hasAttribute(ArmorAttributeType.ARMOR)) {
			return getAttributes().getAttribute(ArmorAttributeType.ARMOR).getMiddle();
		} else if (getAttributes().hasAttribute(ArmorAttributeType.DAMAGE)) {
			return getAttributes().getAttribute(ArmorAttributeType.DAMAGE).getMiddle();
		} else {
			Utils.log.info("Armor did not have either stat for repair cost?");
		}
		return 1000;
	}

	@Override
	protected void onItemBreak(Player player) {
		HealthHandler.updatePlayerHP(player);
	}
	
	public static boolean isArmor(ItemStack item) {
		return ItemArmorHelmet.isHelmet(item) || ItemArmorChestplate.isChestplate(item)
				|| ItemArmorLeggings.isLeggings(item) || ItemArmorBoots.isBoots(item);
	}
}
