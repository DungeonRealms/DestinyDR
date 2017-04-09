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
		super(ItemType.PICKAXE);
	}
	
	public ItemPickaxe(int tier) {
		this();
		this.setLevel(tier * 20);
	}
	
	public ItemPickaxe(ItemStack item) {
		super(item);
	}

	@Override
	public void onLevelUp(Player p) {
		Achievements.getInstance().giveAchievement(p.getUniqueId(), MiningTier.getTierByLevel(getLevel()).getAchievement());
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
