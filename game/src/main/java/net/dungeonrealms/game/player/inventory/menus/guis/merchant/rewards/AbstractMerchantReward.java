package net.dungeonrealms.game.player.inventory.menus.guis.merchant.rewards;

import net.dungeonrealms.game.player.inventory.menus.guis.merchant.ingredients.AbstractMerchantIngredient;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Rar349 on 8/10/2017.
 */
public abstract class AbstractMerchantReward {

    protected Map<AbstractMerchantIngredient, Integer> ingredients = new HashMap<>();


    public abstract void init();

    public abstract void giveReward(Player player, int amountToPurchase);

    public boolean takeRequirements(Player player, int amountToPurchase) {
        ItemStack[] contents = player.getInventory().getStorageContents();
        for(Map.Entry<AbstractMerchantIngredient, Integer> entry : ingredients.entrySet()) {
            AbstractMerchantIngredient ingredient = entry.getKey();
            int toTake = entry.getValue() * amountToPurchase;
            for (int k = 0; k < contents.length; k++) {
                ItemStack inSlot = contents[k];
                if (inSlot == null) continue;
                if(!ingredient.isType(inSlot)) continue;

                if (toTake >= inSlot.getAmount()) {
                    toTake -= inSlot.getAmount();
                    contents[k] = null;
                    if (toTake <= 0) break;
                    continue;
                }

                inSlot.setAmount(inSlot.getAmount() - toTake);
                toTake = 0;
                break;
            }

            if(toTake > 0) return false;

        }

        player.getInventory().setStorageContents(contents);
        player.updateInventory();
        return true;
    }

    public abstract boolean canPurchase(Player player, int amountToPurchase);

    public boolean canAfford(Player player, int amountToPurchase) {

            ItemStack[] contents = player.getInventory().getStorageContents();
            for (Map.Entry<AbstractMerchantIngredient, Integer> entry : ingredients.entrySet()) {
                AbstractMerchantIngredient ingredient = entry.getKey();
                int toTake = entry.getValue() * amountToPurchase;
                for (int k = 0; k < contents.length; k++) {
                    ItemStack inSlot = contents[k];
                    if (inSlot == null) continue;
                    inSlot = inSlot.clone();
                    contents[k] = inSlot;
                    if (!ingredient.isType(inSlot)) continue;

                    if (toTake >= inSlot.getAmount()) {
                        toTake -= inSlot.getAmount();
                        if (toTake == 0) break;
                        continue;
                    }

                    inSlot.setAmount(inSlot.getAmount() - toTake);
                    toTake = 0;
                    break;
                }

                if (toTake > 0) return false;
            }

            return true;

    }

    public List<String> getIngredients() {

        if(ingredients.isEmpty()) {
            init();
            if(ingredients.isEmpty()) {
                System.out.println("INGREDIENTS IS EMPTY!");
                return null;
            }
        }

        List<String> toReturn = new ArrayList<>();
        for(Map.Entry<AbstractMerchantIngredient, Integer> entry : ingredients.entrySet()) {
            toReturn.add(entry.getValue() + "x " + entry.getKey().getName());
        }

        return toReturn;
    }

    public abstract List<String> getLore();

    public abstract MaterialData getDisplay();

    public int getMaxCanAfford(Player player) {
        int k = 0;
        while(canAfford(player, k + 1)) k++;
        return k;
    }

    public int getNumberOfItemsPerPurchase() {
        return 1;
    }


}
