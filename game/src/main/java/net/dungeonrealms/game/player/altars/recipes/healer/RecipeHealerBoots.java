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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by SecondAmendment on 11/22/2017.
 */
public class RecipeHealerBoots extends AbstractRecipe {

    ItemArmorBoots boots;
    String lore = CC.GrayB + "Set Bonus: " + CC.Reset + CC.LightPurple + "Healer";

    @Override
    public boolean checkRecipe(Altars altarType) {
        Altar altar = AltarManager.getAltar(altarType);
        if(altar == null) return false;
        List<Integer> usedIndices = new ArrayList<>(Arrays.asList(0,1,3,5,7));
        for(int index = 0; index < altarType.getNodeSize(); index++) {
            ItemStack onNode = altar.getItemStack(index);
            boolean isUsedPlinth = usedIndices.contains(index);
            if((onNode != null && !isUsedPlinth) || (onNode == null && isUsedPlinth)) return false;
            if(onNode == null) continue;
            if(index == 0){
                if (!ItemArmorBoots.isBoots(onNode)) return false;
                boots = new ItemArmorBoots(onNode.clone());
            }
            else if(index == 1) {
                if (!ItemOrcTooth.isType(onNode, ItemType.ITEM_ORC_TOOTH)) return false;
                //ItemOrcTooth orcTooth = new ItemOrcTooth(onNode);
            }
            else if(index == 3) {
                if(!ItemWitchWart.isType(onNode, ItemType.ITEM_WITCH_WART)) return false;
                //ItemWitchWart witchWart = new ItemWitchWart(onNode);
            }
            else if(index == 5) {
                if (!ItemRatSkin.isType(onNode, ItemType.ITEM_RAT_SKIN)) return false;
                //ItemRatSkin ratSkin = new ItemRatSkin(onNode);
            }
            else if(index == 7) {
                if (!ItemSpiderSilk.isType(onNode, ItemType.ITEM_SPIDER_SILK)) return false;
                //ItemSpiderSilk spiderSilk = new ItemSpiderSilk(onNode);
            }
        }
        return true;
    }

    @Override
    public void giveReward(Player player) {
        //Custom Leather Color CustomId
        boots.setTagString("customId", "healert" + boots.getTier().getTierId());
        boots.setTagString("setBonus", "healer");
        boots.setCustomLore(lore);
        GameAPI.giveOrDropItem(player, boots.generateItem());
    }

    @Override
    public String getRewardDisplayName() {
        return "Healer Boots";
    }

    @Override
    public String getRewardDescription() {
        return lore;
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