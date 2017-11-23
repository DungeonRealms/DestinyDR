package net.dungeonrealms.game.player.altars.recipes.healer;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.*;
import net.dungeonrealms.game.player.altars.Altar;
import net.dungeonrealms.game.player.altars.AltarManager;
import net.dungeonrealms.game.player.altars.Altars;
import net.dungeonrealms.game.player.altars.items.recipeitems.*;
import net.dungeonrealms.game.player.altars.recipes.AbstractRecipe;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by SecondAmendment on 11/22/2017.
 */
public class RecipeHealerLeggings extends AbstractRecipe {

    ItemArmorLeggings leggings;
    String lore;

    @Override
    public boolean checkRecipe(Altars altarType) {
        Altar altar = AltarManager.getAltar(altarType);
        if(altar == null) return false;
        List<Integer> usedIndices = new ArrayList<>(Arrays.asList(1,3,5,7));
        for(int index = 0; index < altarType.getNodeSize(); index++) {
            ItemStack onNode = altar.getItemStack(index);
            boolean isUsedPlinth = usedIndices.contains(index);
            if(onNode == null && isUsedPlinth) return false;
            if(onNode == null) continue;
            if(index == 0){
                if (!ItemArmorLeggings.isLeggings(onNode)) return false;
                leggings = new ItemArmorLeggings(onNode.clone());
            }
            else if(index == 1) {
                if (!ItemMageCocktail.isType(onNode, ItemType.ITEM_MAGE_COCKTAIL)) return false;
                //ItemMageCocktail mageCocktail = new ItemMageCocktail(onNode);
            }
            else if(index == 3) {
                if(!ItemGoldenCharm.isType(onNode, ItemType.ITEM_GOLDEN_CHARM)) return false;
                //ItemGoldenCharm goldenCharm = new ItemGoldenCharm(onNode);
            }
            else if(index == 5) {
                if (!ItemWitherEssence.isType(onNode, ItemType.ITEM_WITHER_ESSENCE)) return false;
                //ItemWitherEssence witherEssence = new ItemWitherEssence(onNode);
            }
            else if(index == 7) {
                if (!ItemFairyDust.isType(onNode, ItemType.ITEM_FAIRY_DUST)) return false;
                //ItemFairyDust fireDust = new ItemFairyDust(onNode);
            }
        }
        return true;
    }

    @Override
    public void giveReward(Player player) {
        leggings.setTagString("customId", "healer");
        leggings.setTagString("setBonus", "healer");
        leggings.addLore(lore = CC.GrayB + "Set Bonus: " + CC.Reset + CC.LightPurple + "Healer");
        GameAPI.giveOrDropItem(player, leggings.generateItem());
    }

    @Override
    public String getRewardDisplayName() {
        return leggings.getCustomName();
    }

    @Override
    public String getRewardDescription() {
        return "Healer Leggings";
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