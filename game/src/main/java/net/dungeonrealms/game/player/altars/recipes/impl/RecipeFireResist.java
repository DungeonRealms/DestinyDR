package net.dungeonrealms.game.player.altars.recipes.impl;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.items.core.*;
import net.dungeonrealms.game.item.items.functional.ItemPeaceOrb;
import net.dungeonrealms.game.player.altars.Altar;
import net.dungeonrealms.game.player.altars.AltarManager;
import net.dungeonrealms.game.player.altars.Altars;
import net.dungeonrealms.game.player.altars.items.ItemFireResistPotion;
import net.dungeonrealms.game.player.altars.items.ItemStrengthPotion;
import net.dungeonrealms.game.player.altars.recipes.AbstractRecipe;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rar349 on 8/4/2017.
 */
public class RecipeFireResist extends AbstractRecipe {
    @Override
    public boolean checkRecipe(Altars altarType) {
        Altar altar = AltarManager.getAltar(altarType);
        if(altar == null) return false;
        List<Integer> usedIndices = new ArrayList<>(Arrays.asList(1,4,6));
        for(int index = 1; index < altarType.getNodeSize(); index++) {
            ItemStack onNode = altar.getItemStack(index);
            boolean isUsedPlinth = usedIndices.contains(index);
            if(onNode == null && isUsedPlinth) return false;
            if(onNode == null) continue;
            if(index == 1) {
                if(!ItemPeaceOrb.isPeaceOrb(onNode)) return false;
            } else if(index == 4) {
                if(!onNode.getType().equals(Material.EMERALD_ORE)) return false;
            } else if(index == 6) {
                if(!ItemWeaponStaff.isStaff(onNode)) return false;
                ItemWeaponStaff staff = new ItemWeaponStaff(onNode);
                if(staff.getTier() != Item.ItemTier.TIER_2) return false;
            }
        }
        return true;
    }

    @Override
    public void giveReward(Player player) {
        ItemFireResistPotion potion = new ItemFireResistPotion(900, 0);
        GameAPI.giveOrDropItem(player, potion.generateItem());
    }

    @Override
    public String getRewardDisplayName() {
        return "Fire Resistance";
    }

    @Override
    public String getRewardDescription() {
        return "Fire Resistance for 15 minutes";
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
