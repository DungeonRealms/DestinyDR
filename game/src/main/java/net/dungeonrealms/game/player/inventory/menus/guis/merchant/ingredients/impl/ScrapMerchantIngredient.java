package net.dungeonrealms.game.player.inventory.menus.guis.merchant.ingredients.impl;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.MiningTier;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.player.inventory.menus.guis.merchant.ingredients.AbstractMerchantIngredient;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 8/13/2017.
 */
public class ScrapMerchantIngredient extends AbstractMerchantIngredient {

    private ScrapTier scrapTier;

    public ScrapMerchantIngredient(ScrapTier scrspTier) {
        this.scrapTier = scrspTier;
    }


    @Override
    public String getName() {
        return ChatColor.stripColor(scrapTier.getName() + " Scrap");
    }

    @Override
    public boolean isType(ItemStack stack) {
        if(stack == null) return false;
        return scrapTier.getRawStack().equals(stack);
    }
}
