package net.dungeonrealms.game.player.inventory.menus.guis.merchant;

import lombok.Getter;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.mechanic.data.MiningTier;
import net.dungeonrealms.game.mechanic.data.PotionTier;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.player.inventory.menus.guis.merchant.rewards.*;
import net.dungeonrealms.game.world.item.Item;

/**
 * Created by Rar349 on 8/10/2017.
 */
@Getter
public enum MerchantItems {

    LEATHER_SCRAP(ChatColor.GREEN + "T1 Scrap", new OreScrapMerchantReward(MiningTier.TIER_1, ScrapTier.TIER1, 1, 2), 0, 9),
    CHAINMAIL_SCRAP(ChatColor.GREEN + "T2 Scrap", new OreScrapMerchantReward(MiningTier.TIER_2, ScrapTier.TIER2, 1, 1), 0, 11),
    IRON_SCRAP(ChatColor.GREEN + "T3 Scrap", new OreScrapMerchantReward(MiningTier.TIER_3, ScrapTier.TIER3, 2, 1), 0, 13),
    DIAMOND_SCRAP(ChatColor.GREEN + "T4 Scrap", new OreScrapMerchantReward(MiningTier.TIER_4, ScrapTier.TIER4, 2, 1), 0, 15),
    GOLD_SCRAP(ChatColor.GREEN + "T5 Scrap", new OreScrapMerchantReward(MiningTier.TIER_5, ScrapTier.TIER5, 2, 1), 0, 17),

    TIER_1_ARM_ENCHANT(ChatColor.GREEN + "T1 Armor Enchant", new ArmorEnchantMerchantReward(Item.ItemTier.TIER_1, 71), 1, 9),
    TIER_2_ARM_ENCHANT(ChatColor.GREEN + "T2 Armor Enchant", new ArmorEnchantMerchantReward(Item.ItemTier.TIER_2, 125), 1, 11),
    TIER_3_ARM_ENCHANT(ChatColor.GREEN + "T3 Armor Enchant", new ArmorEnchantMerchantReward(Item.ItemTier.TIER_3, 100), 1, 13),
    TIER_4_ARM_ENCHANT(ChatColor.GREEN + "T4 Armor Enchant", new ArmorEnchantMerchantReward(Item.ItemTier.TIER_4, 80), 1, 15),
    TIER_5_ARM_ENCHANT(ChatColor.GREEN + "T5 Armor Enchant", new ArmorEnchantMerchantReward(Item.ItemTier.TIER_5, 30), 1, 17),

    TIER_1_WEP_ENCHANT(ChatColor.GREEN + "T1 Weapon Enchant", new WeaponEnchantMerchantReward(Item.ItemTier.TIER_1, 81), 2, 9),
    TIER_2_WEP_ENCHANT(ChatColor.GREEN + "T2 Weapon Enchant", new WeaponEnchantMerchantReward(Item.ItemTier.TIER_2, 143), 2, 11),
    TIER_3_WEP_ENCHANT(ChatColor.GREEN + "T3 Weapon Enchant", new WeaponEnchantMerchantReward(Item.ItemTier.TIER_3, 110), 2, 13),
    TIER_4_WEP_ENCHANT(ChatColor.GREEN + "T4 Weapon Enchant", new WeaponEnchantMerchantReward(Item.ItemTier.TIER_4, 88), 2, 15),
    TIER_5_WEP_ENCHANT(ChatColor.GREEN + "T5 Weapon Enchant", new WeaponEnchantMerchantReward(Item.ItemTier.TIER_5, 33), 2, 17),

    TIER_2_POTION(ChatColor.GREEN + "T2 Potion", new PotionMerchantReward(PotionTier.TIER_2, PotionTier.TIER_1, 5,false), 3, 10),
    TIER_3_POTION(ChatColor.GREEN + "T3 Potion", new PotionMerchantReward(PotionTier.TIER_3, PotionTier.TIER_2, 5,false), 3, 12),
    TIER_4_POTION(ChatColor.GREEN + "T4 Potion", new PotionMerchantReward(PotionTier.TIER_4, PotionTier.TIER_3, 5,false), 3, 14),
    TIER_5_POTION(ChatColor.GREEN + "T5 Potion", new PotionMerchantReward(PotionTier.TIER_5, PotionTier.TIER_4, 5,false), 3, 16),

    TIER_2_SPLASH_POTION(ChatColor.GREEN + "T2 Splash Potion", new PotionMerchantReward(PotionTier.TIER_2, PotionTier.TIER_1, 5,true), 4, 10),
    TIER_3_SPLASH_POTION(ChatColor.GREEN + "T3 Splash Potion", new PotionMerchantReward(PotionTier.TIER_3, PotionTier.TIER_2, 5,true), 4, 12),
    TIER_4_SPLASH_POTION(ChatColor.GREEN + "T4 Splash Potion", new PotionMerchantReward(PotionTier.TIER_4, PotionTier.TIER_3, 5,true), 4, 14),
    TIER_5_SPLASH_POTION(ChatColor.GREEN + "T5 Splash Potion", new PotionMerchantReward(PotionTier.TIER_5, PotionTier.TIER_4, 5,true), 4, 16),

    TIER_2_SCRAP(ChatColor.GREEN + "T2 Scrap", new ScrapMerchantReward(ScrapTier.TIER2, ScrapTier.TIER1, 2, 1), 5, 10),
    TIER_3_SCRAP(ChatColor.GREEN + "T3 Scrap", new ScrapMerchantReward(ScrapTier.TIER3, ScrapTier.TIER2, 2, 1), 5, 12),
    TIER_4_SCRAP(ChatColor.GREEN + "T4 Scrap", new ScrapMerchantReward(ScrapTier.TIER4, ScrapTier.TIER3, 2, 1), 5, 14),
    TIER_5_SCRAP(ChatColor.GREEN + "T5 Scrap", new ScrapMerchantReward(ScrapTier.TIER5, ScrapTier.TIER4, 3, 1), 5, 16),



    ;


    String displayName;
    AbstractMerchantReward reward;
    int guiPageNumber;
    int guiSlot;

    MerchantItems(String displayName,AbstractMerchantReward reward, int guiPageNumber, int guiSlot) {
        this.displayName = displayName;
        this.reward = reward;
        this.guiPageNumber = guiPageNumber;
        this.guiSlot = guiSlot;
    }

    public static int getNumberOfPages() {
         int maxPage = 0;
         for(MerchantItems item : MerchantItems.values()) {
             if(item.getGuiPageNumber() > maxPage) maxPage =item.getGuiPageNumber();
         }
         return maxPage;
    }
}
