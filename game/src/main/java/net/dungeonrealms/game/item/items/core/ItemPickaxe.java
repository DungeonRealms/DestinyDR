package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mechanic.data.MiningTier;
import net.dungeonrealms.game.mechanic.data.ProfessionTier;
import net.dungeonrealms.game.world.item.Item.PickaxeAttributeType;

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

	@Override
	public ProfessionTier getProfessionTier() {
		return MiningTier.getTierByLevel(getLevel());
	}
}
