package net.dungeonrealms.game.player.altars.recipes.impl;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.items.core.ItemWeaponAxe;
import net.dungeonrealms.game.item.items.core.ItemWeaponBow;
import net.dungeonrealms.game.item.items.core.ItemWeaponPolearm;
import net.dungeonrealms.game.item.items.core.ItemWeaponSword;
import net.dungeonrealms.game.player.altars.Altar;
import net.dungeonrealms.game.player.altars.AltarManager;
import net.dungeonrealms.game.player.altars.Altars;
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
public class T4RecipeDPS extends AbstractRecipe {
    @Override
    public boolean checkRecipe(Altars altarType) {
        Altar altar = AltarManager.getAltar(altarType);
        if(altar == null) return false;
        List<Integer> usedIndices = new ArrayList<>(Arrays.asList(1,3,5,7));
        for(int index = 1; index < altarType.getNodeSize(); index++) {
            ItemStack onNode = altar.getItemStack(index);
            boolean isUsedPlinth = usedIndices.contains(index);
            if(onNode == null && isUsedPlinth) return false;
            if(onNode == null) continue;
            if(index == 1) {
                if(!ItemWeaponBow.isBow(onNode)) return false;
                ItemWeaponBow bow = new ItemWeaponBow(onNode);
                if(bow.getTier() != Item.ItemTier.TIER_4) return false;
            } else if(index == 3) {
                if(!ItemWeaponSword.isSword(onNode)) return false;
                ItemWeaponSword sword = new ItemWeaponSword(onNode);
                if(sword.getTier() != Item.ItemTier.TIER_4) return false;
            } else if(index == 5) {
                if(!ItemWeaponAxe.isAxe(onNode)) return false;
                ItemWeaponAxe axe = new ItemWeaponAxe(onNode);
                if(axe.getTier() != Item.ItemTier.TIER_4) return false;
            } else if(index == 7) {
                if(!ItemWeaponPolearm.isPolearm(onNode)) return false;
                ItemWeaponPolearm polearm = new ItemWeaponPolearm(onNode);
                if(polearm.getTier() != Item.ItemTier.TIER_4) return false;
            }
        }
        return true;
    }

    @Override
    public void giveReward(Player player) {
        ItemStrengthPotion potion = new ItemStrengthPotion(600, 1);
        GameAPI.giveOrDropItem(player, potion.generateItem());
    }

    @Override
    public String getRewardDisplayName() {
        return "+35% DPS";
    }

    @Override
    public String getRewardDescription() {
        return "+35% DPS for 10 minutes";
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
