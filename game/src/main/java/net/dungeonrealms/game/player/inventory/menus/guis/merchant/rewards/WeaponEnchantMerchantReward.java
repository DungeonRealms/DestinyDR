package net.dungeonrealms.game.player.inventory.menus.guis.merchant.rewards;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.items.functional.ItemEnchantArmor;
import net.dungeonrealms.game.item.items.functional.ItemEnchantWeapon;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
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
public class WeaponEnchantMerchantReward extends AbstractMerchantReward {

    private Item.ItemTier enchantTier;
    private int scrapAmountRequired;

    public WeaponEnchantMerchantReward(Item.ItemTier enchantTier, int scrapAmountRequired) {
        this.enchantTier = enchantTier;
        this.scrapAmountRequired = scrapAmountRequired;
    }


    @Override
    public void init() {
        ScrapTier required = enchantTier.equals(Item.ItemTier.TIER_5) ? ScrapTier.TIER5 : enchantTier.equals(Item.ItemTier.TIER_4) ? ScrapTier.TIER4 : enchantTier.equals(Item.ItemTier.TIER_3) ? ScrapTier.TIER3 : enchantTier.equals(Item.ItemTier.TIER_2) ? ScrapTier.TIER2 : ScrapTier.TIER1;
        this.ingredients.put(new ScrapMerchantIngredient(required), scrapAmountRequired);
    }

    @Override
    public void giveReward(Player player, int amountToPurchase) {
        for(int k = 0; k < amountToPurchase; k++) {
            ItemEnchantWeapon toGive = new ItemEnchantWeapon(enchantTier);
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
        toReturn.add("1x Tier " + enchantTier.getId() + " Weapon Enchant");
        //toReturn.add("2x Leather Scrap");
        return toReturn;
    }

    @Override
    public MaterialData getDisplay() {
        ItemStack stack = new ItemStack(Material.EMPTY_MAP, 1);
        return stack.getData();
    }

    @Override
    public int getNumberOfItemsPerPurchase() {
        return 1;
    }

}
