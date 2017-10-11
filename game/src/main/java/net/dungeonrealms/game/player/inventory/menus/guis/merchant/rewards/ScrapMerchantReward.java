package net.dungeonrealms.game.player.inventory.menus.guis.merchant.rewards;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.items.functional.ItemScrap;
import net.dungeonrealms.game.item.items.functional.PotionItem;
import net.dungeonrealms.game.mechanic.data.PotionTier;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.player.inventory.menus.guis.merchant.ingredients.impl.PotionMerchantIngredient;
import net.dungeonrealms.game.player.inventory.menus.guis.merchant.ingredients.impl.ScrapMerchantIngredient;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rar349 on 8/10/2017.
 */
public class ScrapMerchantReward extends AbstractMerchantReward {

    private ScrapTier scrapTierToGive, scrapTierToTake;
    private int amountToGive, amountToTake;

    public ScrapMerchantReward(ScrapTier toGive, ScrapTier toTake, int amountToGive, int amountToTake) {
        this.scrapTierToGive = toGive;
        this.scrapTierToTake = toTake;
        this.amountToGive = amountToGive;
        this.amountToTake = amountToTake;
    }


    @Override
    public void init() {
        this.ingredients.put(new ScrapMerchantIngredient(scrapTierToTake), amountToTake);
    }

    @Override
    public void giveReward(Player player, int amountToPurchase) {
            ItemScrap toGive = new ItemScrap(scrapTierToGive);
            ItemStack stack = toGive.generateItem();
            stack.setAmount(amountToGive * amountToPurchase);
            GameAPI.giveOrDropItem(player, stack);
    }

    @Override
    public boolean canPurchase(Player player, int amountToPurchase) {
        return true;
    }


    @Override
    public List<String> getLore() {
        List<String> toReturn = new ArrayList<>();
        toReturn.add(ChatColor.stripColor(scrapTierToGive.getName()) + " Scrap");
        return toReturn;
    }

    @Override
    public MaterialData getDisplay() {
        return scrapTierToGive.getRawStack().getData();
    }

    @Override
    public int getNumberOfItemsPerPurchase() {
        return amountToGive;
    }

}
