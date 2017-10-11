package net.dungeonrealms.game.player.altars.recipes.impl;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.items.core.*;
import net.dungeonrealms.game.player.altars.Altar;
import net.dungeonrealms.game.player.altars.AltarManager;
import net.dungeonrealms.game.player.altars.Altars;
import net.dungeonrealms.game.player.altars.items.ItemResistancePotion;
import net.dungeonrealms.game.player.altars.items.ItemStrengthPotion;
import net.dungeonrealms.game.player.altars.recipes.AbstractRecipe;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rar349 on 8/4/2017.
 */
public class T3RecipeArmor extends AbstractRecipe {
    @Override
    public boolean checkRecipe(Altars altarType) {
        Altar altar = AltarManager.getAltar(altarType);
        if(altar == null) return false;
        List<Integer> usedIndices = new ArrayList<>(Arrays.asList(2,4,6,8));
        for(int index = 1; index < altarType.getNodeSize(); index++) {
            ItemStack onNode = altar.getItemStack(index);
            boolean isUsedPlinth = usedIndices.contains(index);
            if(onNode == null && isUsedPlinth) return false;
            if(onNode == null) continue;
            if(index == 2) {
                if(!ItemArmorHelmet.isHelmet(onNode)) return false;
                ItemArmorHelmet helmet = new ItemArmorHelmet(onNode);
                if(helmet.getTier() != Item.ItemTier.TIER_3) return false;
            } else if(index == 4) {
                if(!ItemArmorChestplate.isChestplate(onNode)) return false;
                ItemArmorChestplate chest = new ItemArmorChestplate(onNode);
                if(chest.getTier() != Item.ItemTier.TIER_3) return false;
            } else if(index == 6) {
                if(!ItemArmorLeggings.isLeggings(onNode)) return false;
                ItemArmorLeggings legs = new ItemArmorLeggings(onNode);
                if(legs.getTier() != Item.ItemTier.TIER_3) return false;
            } else if(index == 8) {
                if(!ItemArmorBoots.isBoots(onNode)) return false;
                ItemArmorBoots boots = new ItemArmorBoots(onNode);
                if(boots.getTier() != Item.ItemTier.TIER_3) return false;
            }
        }
        return true;
    }

    @Override
    public void giveReward(Player player) {
        ItemResistancePotion potion = new ItemResistancePotion(600, 0);
        GameAPI.giveOrDropItem(player, potion.generateItem());
    }

    @Override
    public String getRewardDisplayName() {
        return "+15% Armor";
    }

    @Override
    public String getRewardDescription() {
        return "+15% Armor for 10 minutes";
    }

    @Override
    public boolean isUnlocked(Player player) {
        return true;
    }

    @Override
    public long getRitualTime() {
        return 4;
    }
}
