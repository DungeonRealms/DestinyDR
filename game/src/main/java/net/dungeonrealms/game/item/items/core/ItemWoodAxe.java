package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mechanic.data.WoodcutTier;
import net.dungeonrealms.game.mechanic.data.ProfessionTier;
import net.dungeonrealms.game.world.item.Item.WoodAxeAttributeType;

import org.bukkit.inventory.ItemStack;

/**
 * WoodAxes
 * @author Kihz
 */
public class ItemWoodAxe extends ProfessionItem {

    public ItemWoodAxe() {
        this(1);
    }

    public ItemWoodAxe(int level) {
        super(ItemType.WOODAXE, level);
    }

    public ItemWoodAxe(ItemStack item) {
        super(item);
    }

    /**
     * Returns the chance of the ore breaking.
     */
    public int getSuccessChance() {
        return (getLevel() - (20 * (getTier().getId() - 1))) * 2 + 50
                + getAttributes().getAttribute(WoodAxeAttributeType.MINING_SUCCESS).getValue();
    }

    public static boolean isWoodAxe(ItemStack item) {
        return isType(item, ItemType.WOODAXE);
    }

    @Override
    public ProfessionTier getProfessionTier() {
        return WoodcutTier.getTierByLevel(getLevel());
    }
}

