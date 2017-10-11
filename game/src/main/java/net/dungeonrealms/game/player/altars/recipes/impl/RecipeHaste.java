package net.dungeonrealms.game.player.altars.recipes.impl;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.item.items.functional.ItemFlightOrb;
import net.dungeonrealms.game.item.items.functional.ItemPeaceOrb;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketItem;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketType;
import net.dungeonrealms.game.player.altars.Altar;
import net.dungeonrealms.game.player.altars.AltarManager;
import net.dungeonrealms.game.player.altars.Altars;
import net.dungeonrealms.game.player.altars.items.ItemFireResistPotion;
import net.dungeonrealms.game.player.altars.items.ItemHastePotion;
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
public class RecipeHaste extends AbstractRecipe {
    @Override
    public boolean checkRecipe(Altars altarType) {
        Altar altar = AltarManager.getAltar(altarType);
        if(altar == null) return false;
        List<Integer> usedIndices = new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7,8));
        for(int index = 1; index < altarType.getNodeSize(); index++) {
            ItemStack onNode = altar.getItemStack(index);
            boolean isUsedPlinth = usedIndices.contains(index);
            if(onNode == null && isUsedPlinth) return false;
            if(onNode == null) continue;
            if(index == 1) {
                if(!onNode.getType().equals(Material.IRON_ORE)) return false;
            } else if(index == 2) {
                if(!PersistentItem.isType(onNode, ItemType.TRINKET)) return false;
                TrinketItem item = new TrinketItem(onNode);
                if(item.getTrinketType() == null || !item.getTrinketType().equals(TrinketType.MINING_GLOVE)) return false;
            } else if(index == 3) {
                if(!onNode.getType().equals(Material.DIAMOND_ORE)) return false;
            } else if(index == 4) {
                if(!PersistentItem.isType(onNode, ItemType.TRINKET)) return false;
                TrinketItem item = new TrinketItem(onNode);
                if(item.getTrinketType() == null || !item.getTrinketType().equals(TrinketType.MINING_GLOVE)) return false;
            } else if(index == 5) {
                if(!ItemFlightOrb.isFlightOrb(onNode)) return false;
            } else if(index == 6) {
                if(!onNode.getType().equals(Material.GOLD_ORE)) return false;
            } else if(index == 7) {
                if(!onNode.getType().equals(Material.WOOD_PICKAXE)) return false;
            } else if(index == 8) {
                if(!PersistentItem.isType(onNode, ItemType.TRINKET)) return false;
                TrinketItem item = new TrinketItem(onNode);
                if(item.getTrinketType() == null || !item.getTrinketType().equals(TrinketType.MINING_GLOVE)) return false;
            }
        }
        return true;
    }

    @Override
    public void giveReward(Player player) {
        ItemHastePotion potion = new ItemHastePotion(300, 0);
        GameAPI.giveOrDropItem(player, potion.generateItem());
    }

    @Override
    public String getRewardDisplayName() {
        return "Haste";
    }

    @Override
    public String getRewardDescription() {
        return "Haste for 5 minutes";
    }

    @Override
    public boolean isUnlocked(Player player) {
        return true;
    }

    @Override
    public long getRitualTime() {
        return 8;
    }
}
