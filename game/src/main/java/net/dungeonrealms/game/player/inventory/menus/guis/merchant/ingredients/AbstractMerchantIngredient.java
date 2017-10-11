package net.dungeonrealms.game.player.inventory.menus.guis.merchant.ingredients;

import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 8/13/2017.
 */
public abstract class AbstractMerchantIngredient {

    public abstract String getName();

    public abstract boolean isType(ItemStack stack);
}
