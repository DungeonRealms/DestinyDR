package net.dungeonrealms.game.player.inventory.menus.guis.merchant.rewards;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.items.functional.ItemEnchantWeapon;
import net.dungeonrealms.game.item.items.functional.PotionItem;
import net.dungeonrealms.game.mechanic.data.PotionTier;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.player.inventory.menus.guis.merchant.ingredients.impl.PotionMerchantIngredient;
import net.dungeonrealms.game.player.inventory.menus.guis.merchant.ingredients.impl.ScrapMerchantIngredient;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rar349 on 8/10/2017.
 */
public class PotionMerchantReward extends AbstractMerchantReward {

    private PotionTier potionTier;
    private PotionTier toTakePotionTier;
    private int potionsRequired;
    private boolean isSplash;

    public PotionMerchantReward(PotionTier potionTier, PotionTier toTakePotionTier,int potionsRequired, boolean isSplash) {
        this.potionTier = potionTier;
        this.toTakePotionTier = toTakePotionTier;
        this.potionsRequired = potionsRequired;
        this.isSplash = isSplash;
    }


    @Override
    public void init() {
        this.ingredients.put(new PotionMerchantIngredient(potionTier,isSplash), potionsRequired);
    }

    @Override
    public void giveReward(Player player, int amountToPurchase) {
        for(int k = 0; k < amountToPurchase; k++) {
            PotionItem toGive = new PotionItem(potionTier);
            toGive.setSplash(isSplash);
            ItemStack stack = toGive.generateItem();
            stack.setAmount(1);
            GameAPI.giveOrDropItem(player, stack);
        }
    }

    @Override
    public boolean canPurchase(Player player, int amountToPurchase) {
        return true;
    }


    @Override
    public List<String> getLore() {
        List<String> toReturn = new ArrayList<>();
        toReturn.add("Tier " + potionTier.getId() + (isSplash ? " Splash Potion" : " Potion"));
        return toReturn;
    }

    @Override
    public MaterialData getDisplay() {
        PotionItem toGive = new PotionItem(potionTier);
        toGive.setSplash(isSplash);
        ItemStack stack = toGive.generateItem();
        return stack.getData();
    }

    @Override
    public int getNumberOfItemsPerPurchase() {
        return 1;
    }

}
