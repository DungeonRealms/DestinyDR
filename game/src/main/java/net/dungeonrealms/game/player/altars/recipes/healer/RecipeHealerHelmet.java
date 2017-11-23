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
public class RecipeHealerHelmet extends AbstractRecipe {

    ItemArmorHelmet helmet;
    String lore;

    @Override
    public boolean checkRecipe(Altars altarType) {
        System.out.println("Checking Recipe");
        Altar altar = AltarManager.getAltar(altarType);
        if(altar == null) return false;
        List<Integer> usedIndices = new ArrayList<>(Arrays.asList(1,3,5,7));
        for(int index = 0; index < altarType.getNodeSize(); index++) {
            ItemStack onNode = altar.getItemStack(index);
            boolean isUsedPlinth = usedIndices.contains(index);
            if(onNode == null && isUsedPlinth) return false;
            if(onNode == null) continue;
                if(index == 0){
                    if (!ItemArmorHelmet.isHelmet(onNode)) return false;
                    System.out.println("Helmet Is THERE");
                    helmet = new ItemArmorHelmet(onNode.clone());
                }
                else if(index == 1) {
                    if (!ItemEyeOfBeholder.isType(onNode, ItemType.ITEM_EYE_OF_BEHOLDER)) return false;
                    System.out.println("EOB IS THERE");
                    //ItemEyeOfBeholder eyeOfBeholder = new ItemEyeOfBeholder(onNode);
                }
                else if(index == 3) {
                    if(!ItemImpEye.isType(onNode, ItemType.ITEM_IMP_EYE)) return false;
                    System.out.println("ImpEye IS THERE");
                    //ItemImpEye impEye = new ItemImpEye(onNode);
                }
                else if(index == 5) {
                    if (!ItemDogTongue.isType(onNode, ItemType.ITEM_DOG_TONGUE)) return false;
                    System.out.println("DogTongue IS THERE");
                    //ItemDogTongue dogTongue = new ItemDogTongue(onNode);
                }
                else if(index == 7) {
                    if (!ItemSpiderEye.isType(onNode, ItemType.ITEM_SPIDER_EYE)) return false;
                    System.out.println("SpiderEye IS THERE");
                    //ItemSpiderEye spiderEye = new ItemSpiderEye(onNode);
                }
        }
        return true;
    }

    @Override
    public void giveReward(Player player) {
        System.out.println("Giving Healer Reward");
        helmet.setTagString("customId", "healert" +  + helmet.getTier().getTierId());
        helmet.setTagString("setBonus", "healer");
        helmet.addLore(lore = CC.GrayB + "Set Bonus: " + CC.Reset + CC.LightPurple + "Healer");
        GameAPI.giveOrDropItem(player, helmet.generateItem());
    }

    @Override
    public String getRewardDisplayName() {
        return "Healer Helmet";
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
