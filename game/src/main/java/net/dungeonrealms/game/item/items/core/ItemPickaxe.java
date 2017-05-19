package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mechanic.data.MiningTier;
import net.dungeonrealms.game.world.item.Item.PickaxeAttributeType;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Pickaxes.
 * @author Kneesnap
 */
public class ItemPickaxe extends ProfessionItem {

	public ItemPickaxe() {
		this(1);
	}
	
	public ItemPickaxe(int level) {
		super(ItemType.PICKAXE);
		this.setLevel(level);
	}
	
	public ItemPickaxe(ItemStack item) {
		super(item);
	}

	@Override
	public void onLevelUp(Player p) {
		Achievements.giveAchievement(p, MiningTier.getTierByLevel(getLevel()).getAchievement());
	}

	@Override
	public void updateItem() {
		MiningTier tier = MiningTier.getTierFromPickaxe(this);
		this.setCustomDisplayName(tier == null ? "Error" : tier.getItemName());
		super.updateItem();
	}

	/**
	 * Returns the chance of the ore breaking.
	 */
	public int getSuccessChance() {
        return (getLevel() - (20 * (getTier().getId() - 1))) * 2 + 50
        		+ getAttributes().getAttribute(PickaxeAttributeType.MINING_SUCCESS).getValue();
    }
	
	public static boolean isPickaxe(ItemStack item) {
		return isType(item, ItemType.PICKAXE);
	}
}
