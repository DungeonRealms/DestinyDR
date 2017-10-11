package net.dungeonrealms.game.player.inventory.menus.guis.merchant.ingredients.impl;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.MiningTier;
import net.dungeonrealms.game.player.inventory.menus.guis.merchant.ingredients.AbstractMerchantIngredient;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 8/13/2017.
 */
public class OreMerchantIngredient extends AbstractMerchantIngredient {

    private MiningTier oreTier;

    public OreMerchantIngredient(MiningTier oreTier) {
        this.oreTier = oreTier;
    }


    @Override
    public String getName() {
        return ChatColor.stripColor(Utils.getItemName(oreTier.createOreItem()));
    }

    @Override
    public boolean isType(ItemStack stack) {
        if(stack == null) return false;
        return oreTier.createOreItem().getType().equals(stack.getType());
    }
}
