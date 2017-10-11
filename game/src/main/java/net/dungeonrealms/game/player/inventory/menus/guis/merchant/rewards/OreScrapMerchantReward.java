package net.dungeonrealms.game.player.inventory.menus.guis.merchant.rewards;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.functional.ItemScrap;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.MiningTier;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.player.inventory.menus.guis.merchant.ingredients.AbstractMerchantIngredient;
import net.dungeonrealms.game.player.inventory.menus.guis.merchant.ingredients.impl.OreMerchantIngredient;
import net.dungeonrealms.game.player.inventory.menus.guis.merchant.rewards.AbstractMerchantReward;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rar349 on 8/10/2017.
 */
public class OreScrapMerchantReward extends AbstractMerchantReward {

    private MiningTier oreTier;
    private ScrapTier scrapTier;
    private int oreAmountRequired;
    private int scrapToGive;

    public OreScrapMerchantReward(MiningTier oreTier, ScrapTier scrapTier, int oreAmountRequired, int scrapToGive) {
        this.oreTier = oreTier;
        this.scrapTier = scrapTier;
        this.oreAmountRequired = oreAmountRequired;
        this.scrapToGive = scrapToGive;
    }


    @Override
    public void init() {
        this.ingredients.put(new OreMerchantIngredient(oreTier), oreAmountRequired);
    }

    @Override
    public void giveReward(Player player, int amountToPurchase) {
        ItemScrap toGive = new ItemScrap(scrapTier);
        ItemStack stack = toGive.generateItem();
        stack.setAmount(scrapToGive * amountToPurchase);
        GameAPI.giveOrDropItem(player, stack);
    }

    @Override
    public boolean canPurchase(Player player, int amountToPurchase) {
        return true;
    }


    @Override
    public List<String> getLore() {
        List<String> toReturn = new ArrayList<>();
        toReturn.add(scrapToGive + "x " + ChatColor.stripColor(scrapTier.getName()) + " Scrap");
        //toReturn.add("2x Leather Scrap");
        return toReturn;
    }

    @Override
    public MaterialData getDisplay() {
        ItemScrap scrap = new ItemScrap(scrapTier);
        ItemStack stack = scrap.generateItem();
        return stack.getData();
    }

    @Override
    public int getNumberOfItemsPerPurchase() {
        return 2;
    }

}
