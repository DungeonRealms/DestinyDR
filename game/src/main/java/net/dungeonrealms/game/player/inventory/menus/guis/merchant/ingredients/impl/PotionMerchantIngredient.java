package net.dungeonrealms.game.player.inventory.menus.guis.merchant.ingredients.impl;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.functional.PotionItem;
import net.dungeonrealms.game.mechanic.data.PotionTier;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.player.inventory.menus.guis.merchant.ingredients.AbstractMerchantIngredient;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 8/13/2017.
 */
public class PotionMerchantIngredient extends AbstractMerchantIngredient {

    private PotionTier potionTier;
    private boolean isSplash;

    public PotionMerchantIngredient(PotionTier potionTier, boolean isSplash) {
        this.potionTier = potionTier;
        this.isSplash = isSplash;
    }


    @Override
    public String getName() {
        return "Tier " + potionTier.getId() + (isSplash ? " Splash Potion" : " Potion");
    }

    @Override
    public boolean isType(ItemStack stack) {
        if(stack == null) return false;
        if(!PersistentItem.isType(stack, ItemType.POTION)) return false;
        PotionItem item = new PotionItem(stack);
        if(isSplash && !item.isSplash()) return false;
        if(!isSplash && item.isSplash()) return false;
        return item.getTier().equals(potionTier);
    }
}
