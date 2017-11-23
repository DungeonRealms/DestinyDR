package net.dungeonrealms.game.player.altars.recipes.healer;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.*;
import net.dungeonrealms.game.item.items.core.setbonus.SetBonuses;
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
public class RecipeHealerChestplate extends AbstractRecipe {

    ItemArmorChestplate chestplate;
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
                if (!ItemArmorChestplate.isChestplate(onNode)) return false;
                chestplate = new ItemArmorChestplate(onNode.clone());
            }
            else if(index == 1) {
                if (!ItemHemitite.isType(onNode, ItemType.ITEM_HEMITITE)) return false;
                //ItemHemitite hemitite = new ItemHemitite(onNode);
            }
            else if(index == 3) {
                if(!ItemSacrificialHeart.isType(onNode, ItemType.ITEM_SACRIFICIAL_HEART)) return false;
                //ItemSacrificialHeart sacrificialHeart = new ItemSacrificialHeart(onNode);
            }
            else if(index == 5) {
                if (!ItemLizardScale.isType(onNode, ItemType.ITEM_LIZARD_SCALE)) return false;
                //ItemLizardScale lizardScale = new ItemLizardScale(onNode);
            }
            else if(index == 7) {
                if (!ItemFireDust.isType(onNode, ItemType.ITEM_FIRE_DUST)) return false;
                //ItemFireDust fireDust = new ItemFireDust(onNode);
            }
        }
        return true;
    }

    @Override
    public void giveReward(Player player) {
        chestplate.setTagString("customId", "healer");
        chestplate.setTagString("setBonus", "healer");
        chestplate.addLore(lore = CC.GrayB + "Set Bonus: " + CC.Reset + CC.LightPurple + "Healer");
        GameAPI.giveOrDropItem(player, chestplate.generateItem());
    }

    @Override
    public String getRewardDisplayName() {
        return chestplate.getCustomName();
    }

    @Override
    public String getRewardDescription() {
        return "Healer Chestplate";
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
