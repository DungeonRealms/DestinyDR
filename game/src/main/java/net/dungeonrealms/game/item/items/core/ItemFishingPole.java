package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mechanic.data.FishingTier;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A basic fishing pole item
 * @author Kneesnap
 */
public class ItemFishingPole extends ProfessionItem {

	public ItemFishingPole() {
		this(1);
	}
	
	public ItemFishingPole(int level) {
		super(ItemType.FISHING_POLE);
		this.setLevel(level);
	}
	
	public ItemFishingPole(ItemStack item) {
		super(item);
	}
	
	@Override
	public void updateItem() {
		FishingTier tier = FishingTier.getTierByLevel(getLevel());
		setCustomDisplayName(tier == null ? "Error" : tier.getItemName());
		getMeta().addEnchant(Enchantment.LURE, 3, false);
        getItem().addEnchantment(Enchantment.LURE, 3);
		super.updateItem();
	}
	
	public static boolean isFishingPole(ItemStack item) {
		return isType(item, ItemType.FISHING_POLE);
	}

	@Override
	public void onLevelUp(Player p) {
		Achievements.giveAchievement(p, FishingTier.getTierByLevel(getLevel()).getAchievement());
	}
}
