package net.dungeonrealms.game.player.altars.recipes.impl;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.functional.ItemFish;
import net.dungeonrealms.game.item.items.functional.ItemFlightOrb;
import net.dungeonrealms.game.item.items.functional.ItemPeaceOrb;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketItem;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketType;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.player.altars.Altar;
import net.dungeonrealms.game.player.altars.AltarManager;
import net.dungeonrealms.game.player.altars.Altars;
import net.dungeonrealms.game.player.altars.items.ItemHastePotion;
import net.dungeonrealms.game.player.altars.items.ItemWaterBreathingPotion;
import net.dungeonrealms.game.player.altars.recipes.AbstractRecipe;
import net.dungeonrealms.game.profession.fishing.FishSpeedBuff;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rar349 on 8/4/2017.
 */
public class RecipeWaterBreathing extends AbstractRecipe {
    @Override
    public boolean checkRecipe(Altars altarType) {
        Altar altar = AltarManager.getAltar(altarType);
        if(altar == null) return false;
        List<Integer> usedIndices = new ArrayList<>(Arrays.asList(2,5,8));
        for(int index = 1; index < altarType.getNodeSize(); index++) {
            ItemStack onNode = altar.getItemStack(index);
            boolean isUsedPlinth = usedIndices.contains(index);
            if(onNode == null && isUsedPlinth) return false;
            if(onNode == null) continue;
            if(index == 2) {
                if(!ItemFish.isFish(onNode)) return false;
                ItemFish fish = new ItemFish(onNode);
                if(fish.getTier() != FishingTier.TIER_1) return false;
            } else if(index == 5) {
                if(!ItemPeaceOrb.isPeaceOrb(onNode))return false;
            } else if(index == 8) {
                if(!ItemFish.isFish(onNode)) return false;
                ItemFish fish = new ItemFish(onNode);
                if(fish.getTier() != FishingTier.TIER_2) return false;
            }
        }
        return true;
    }

    @Override
    public void giveReward(Player player) {
        ItemWaterBreathingPotion potion = new ItemWaterBreathingPotion(900, 0);
        GameAPI.giveOrDropItem(player, potion.generateItem());
    }

    @Override
    public String getRewardDisplayName() {
        return "Water Breathing";
    }

    @Override
    public String getRewardDescription() {
        return "Water Breathing for 15 minutes";
    }

    @Override
    public boolean isUnlocked(Player player) {
        return true;
    }

    @Override
    public long getRitualTime() {
        return 3;
    }
}
