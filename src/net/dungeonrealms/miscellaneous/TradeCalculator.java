package net.dungeonrealms.miscellaneous;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.items.repairing.RepairAPI;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.profession.Mining;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kieran on 11/5/2015.
 */
public class TradeCalculator {

    public static List<ItemStack> calculateMerchantOffer(List<ItemStack> player_Offer) {
        List<ItemStack> merchant_offer = new ArrayList<>();
        List<ItemStack> to_remove = new ArrayList<>();
        int t1_scraps = 0, t2_scraps = 0, t3_scraps = 0, t4_scraps = 0, t5_scraps = 0;
        int t1_ore = 0, t2_ore = 0, t3_ore = 0, t4_ore = 0, t5_ore = 0;
        int t1_pot = 0, t2_pot = 0, t3_pot = 0, t4_pot = 0 , t5_pot = 0;
        int t1_Splash_pot = 0, t2_Splash_pot = 0, t3_Splash_pot = 0, t4_Splash_pot = 0 , t5_Splash_pot = 0;


        //TODO: Skill Scrolls (Professions)
        //TODO: Potions

        for (ItemStack is : player_Offer) {
            if (is == null || is.getType() == Material.AIR) {
                continue;
            }

            if (is.getType() == Material.POTION) {
                net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(is);
                if (nmsStack != null && nmsStack.getTag() != null && nmsStack.getTag().hasKey("itemTier")) {
                    switch (nmsStack.getTag().getInt("itemTier")) {
                        case 1:
                            if (nmsStack.getTag().getString("type").equalsIgnoreCase("healthPotion")) {
                                t1_pot += is.getAmount();
                            } else {
                                t1_Splash_pot += is.getAmount();
                            }
                            break;
                        case 2:
                            if (nmsStack.getTag().getString("type").equalsIgnoreCase("healthPotion")) {
                                t2_pot += is.getAmount();
                            } else {
                                t2_Splash_pot += is.getAmount();
                            }
                            break;
                        case 3:
                            if (nmsStack.getTag().getString("type").equalsIgnoreCase("healthPotion")) {
                                t3_pot += is.getAmount();
                            } else {
                                t3_Splash_pot += is.getAmount();
                            }
                            break;
                        case 4:
                            if (nmsStack.getTag().getString("type").equalsIgnoreCase("healthPotion")) {
                                t4_pot += is.getAmount();
                            } else {
                                t4_Splash_pot += is.getAmount();
                            }
                            break;
                        case 5:
                            if (nmsStack.getTag().getString("type").equalsIgnoreCase("healthPotion")) {
                                t5_pot += is.getAmount();
                            } else {
                                t5_Splash_pot += is.getAmount();
                            }
                            break;
                    }
                    to_remove.add(is);
                }
            }
            if (is.getType() == Material.COAL_ORE || is.getType() == Material.EMERALD_ORE || is.getType() == Material.IRON_ORE
                    || is.getType() == Material.DIAMOND_ORE || is.getType() == Material.GOLD_ORE) {
                int tier = Mining.getBlockTier(is.getType());
                switch (tier) {
                    case 1:
                        t1_ore += is.getAmount();
                        break;
                    case 2:
                        t2_ore += is.getAmount();
                        break;
                    case 3:
                        t3_ore += is.getAmount();
                        break;
                    case 4:
                        t4_ore += is.getAmount();
                        break;
                    case 5:
                        t5_ore += is.getAmount();
                        break;
                }
                to_remove.add(is);
            }
        }

        for (ItemStack is : player_Offer) {
            if (is == null || is.getType() == Material.AIR) {
                continue;
            }
            if (RepairAPI.isItemArmorScrap(is)) {
                int tier = RepairAPI.getScrapTier(is);
                switch (tier) {
                    case 1:
                        t1_scraps += is.getAmount();
                        break;
                    case 2:
                        t2_scraps += is.getAmount();
                        break;
                    case 3:
                        t3_scraps += is.getAmount();
                        break;
                    case 4:
                        t4_scraps += is.getAmount();
                        break;
                    case 5:
                        t5_scraps += is.getAmount();
                        break;
                }
                to_remove.add(is);
            }
        }

        to_remove.forEach(player_Offer::remove);

        for (ItemStack is : player_Offer) {
            if (is == null || is.getType() == Material.AIR) {
                continue;
            }
            int tier = RepairAPI.getArmorOrWeaponTier(is);
            if (RepairAPI.isItemArmorOrWeapon(is)) {
                int payout = 0;
                net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(is);
                NBTTagCompound tag = nmsItem.getTag();
                if (tag.hasKey("type")) {
                    if (tag.getString("type").equalsIgnoreCase("weapon")) {
                        payout = 2;
                    }
                    if (tag.getString("type").equalsIgnoreCase("armor")) {
                        payout = 2;
                    }
                    switch (tier) {
                        case 1:
                            ItemStack scrap1 = ItemManager.createArmorScrap(2);
                            scrap1.setAmount(payout);
                            merchant_offer.add(scrap1);
                            break;
                        case 2:
                            ItemStack scrap2 = ItemManager.createArmorScrap(3);
                            scrap2.setAmount(payout);
                            merchant_offer.add(scrap2);
                            break;
                        case 3:
                            ItemStack scrap3 = ItemManager.createArmorScrap(4);
                            scrap3.setAmount(payout);
                            merchant_offer.add(scrap3);
                            break;
                        case 4:
                            ItemStack scrap4 = ItemManager.createArmorScrap(5);
                            scrap4.setAmount(payout);
                            merchant_offer.add(scrap4);
                            break;
                        case 5:
                            ItemStack scrap5 = ItemManager.createArmorScrap(5);
                            scrap5.setAmount(payout * 2);
                            merchant_offer.add(scrap5);
                            break;
                    }
                }
            }
        }

        //TODO: Gem Pouches
        if (t1_pot > 0) {
            while (t1_pot >= 6) {
                t1_pot -= 6;
                ItemStack pot = ItemManager.createHealthPotion(2, true, false);
                merchant_offer.add(pot);
            }
        }
        if (t2_pot > 0) {
            while (t2_pot >= 5) {
                t2_pot -= 5;
                ItemStack pot = ItemManager.createHealthPotion(3, true, false);
                merchant_offer.add(pot);
            }
        }
        if (t3_pot > 0) {
            while (t3_pot >= 3) {
                t3_pot -= 3;
                ItemStack pot = ItemManager.createHealthPotion(4, true, false);
                merchant_offer.add(pot);
            }
        }
        if (t4_pot > 0) {
            while (t4_pot >= 3) {
                t4_pot -= 3;
                ItemStack pot = ItemManager.createHealthPotion(5, true, false);
                merchant_offer.add(pot);
            }
        }
        if (t5_pot > 0) {
            while (t5_pot >= 2) {
                t5_pot -= 2;
                ItemStack pot = ItemManager.createHealthPotion(5, false, false);
                merchant_offer.add(pot);
                ItemStack gems = BankMechanics.createBankNote(100);
                merchant_offer.add(gems);
            }
        }

        if (t1_Splash_pot > 0) {
            while (t1_Splash_pot >= 6) {
                t1_Splash_pot -= 6;
                ItemStack pot = ItemManager.createHealthPotion(2, true, true);
                merchant_offer.add(pot);
            }
        }
        if (t2_Splash_pot > 0) {
            while (t2_Splash_pot >= 5) {
                t2_Splash_pot -= 5;
                ItemStack pot = ItemManager.createHealthPotion(3, true, true);
                merchant_offer.add(pot);
            }
        }
        if (t3_Splash_pot > 0) {
            while (t3_Splash_pot >= 3) {
                t3_Splash_pot -= 3;
                ItemStack pot = ItemManager.createHealthPotion(4, true, true);
                merchant_offer.add(pot);
            }
        }
        if (t4_Splash_pot > 0) {
            while (t4_Splash_pot >= 3) {
                t4_Splash_pot -= 3;
                ItemStack pot = ItemManager.createHealthPotion(5, true, true);
                merchant_offer.add(pot);
            }
        }
        if (t5_Splash_pot > 0) {
            while (t5_Splash_pot >= 2) {
                t5_Splash_pot -= 2;
                ItemStack pot = ItemManager.createHealthPotion(5, false, true);
                merchant_offer.add(pot);
                ItemStack gems = BankMechanics.createBankNote(200);
                merchant_offer.add(gems);
            }
        }


        if (t1_ore > 0) {
            while (t1_ore >= 100) {
                t1_ore -= 100;
                ItemStack pouch = BankMechanics.getInstance().createGemPouch(1, 0);
                merchant_offer.add(pouch);
            }

            int payout = t1_ore * 2;
            while (payout > 64) {
                payout -= 64;
                ItemStack scrap = ItemManager.createArmorScrap(1);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
            }
            ItemStack scrap = ItemManager.createArmorScrap(1);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        if (t2_ore > 0) {
            while (t2_ore >= 150) {
                t2_ore -= 150;
                ItemStack pouch = BankMechanics.getInstance().createGemPouch(2, 0);
                merchant_offer.add(pouch);
            }

            while (t2_ore >= 70) {
                t2_ore -= 70;
                ItemStack pouch = BankMechanics.getInstance().createGemPouch(1, 0);
                merchant_offer.add(pouch);
            }

            int payout = t2_ore;
            while (payout > 64) {
                payout -= 64;
                ItemStack scrap = ItemManager.createArmorScrap(2);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
            }
            ItemStack scrap = ItemManager.createArmorScrap(2);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        if (t3_ore > 0) {
            while (t3_ore >= 200) {
                t3_ore -= 200;
                ItemStack pouch = BankMechanics.getInstance().createGemPouch(3, 0);
                merchant_offer.add(pouch);
            }
            while (t3_ore >= 100) {
                t3_ore -= 100;
                ItemStack pouch = BankMechanics.getInstance().createGemPouch(2, 0);
                merchant_offer.add(pouch);
            }
            while (t3_ore >= 40) {
                t3_ore -= 40;
                ItemStack pouch = BankMechanics.getInstance().createGemPouch(1, 0);
                merchant_offer.add(pouch);
            }

            int payout = t3_ore / 2;
            while (payout > 64) {
                payout -= 64;
                ItemStack scrap = ItemManager.createArmorScrap(3);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
            }
            ItemStack scrap = ItemManager.createArmorScrap(3);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        if (t4_ore > 0) {
            while (t4_ore >= 140) {
                t4_ore -= 140;
                ItemStack pouch = BankMechanics.getInstance().createGemPouch(3, 0);
                merchant_offer.add(pouch);
            }
            while (t4_ore >= 80) {
                t4_ore -= 80;
                ItemStack pouch = BankMechanics.getInstance().createGemPouch(2, 0);
                merchant_offer.add(pouch);
            }
            while (t4_ore >= 35) {
                t4_ore -= 35;
                ItemStack pouch = BankMechanics.getInstance().createGemPouch(1, 0);
                merchant_offer.add(pouch);
            }

            int payout = t4_ore / 2;
            while (payout > 64) {
                payout -= 64;
                ItemStack scrap = ItemManager.createArmorScrap(4);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
            }
            ItemStack scrap = ItemManager.createArmorScrap(4);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        if (t5_ore > 0) {
            while (t5_ore >= 80) {
                t5_ore -= 80;
                ItemStack pouch = BankMechanics.getInstance().createGemPouch(4, 0);
                merchant_offer.add(pouch);
            }
            while (t5_ore >= 60) {
                t5_ore -= 60;
                ItemStack pouch = BankMechanics.getInstance().createGemPouch(3, 0);
                merchant_offer.add(pouch);
            }
            while (t5_ore >= 40) {
                t5_ore -= 40;
                ItemStack pouch = BankMechanics.getInstance().createGemPouch(2, 0);
                merchant_offer.add(pouch);
            }
            while (t5_ore >= 20) {
                t5_ore -= 20;
                ItemStack pouch = BankMechanics.getInstance().createGemPouch(1, 0);
                merchant_offer.add(pouch);
            }

            int payout = t5_ore / 2;
            while (payout > 64) {
                payout -= 64;
                ItemStack scrap = ItemManager.createArmorScrap(5);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
            }
            ItemStack scrap = ItemManager.createArmorScrap(5);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        
        
        
        
        
        if (t1_ore > 0) {
            int payout = t1_ore * 2;
            while (payout > 64) {
                payout -= 64;
                ItemStack scrap = ItemManager.createArmorScrap(1);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
            }
            ItemStack scrap = ItemManager.createArmorScrap(1);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        if (t2_ore > 0) {
            int payout = t2_ore;
            while (payout > 64) {
                payout -= 64;
                ItemStack scrap = ItemManager.createArmorScrap(2);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
            }
            ItemStack scrap = ItemManager.createArmorScrap(2);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        if (t3_ore > 0) {
            int payout = t3_ore / 2;
            while (payout > 64) {
                payout -= 64;
                ItemStack scrap = ItemManager.createArmorScrap(3);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
            }
            ItemStack scrap = ItemManager.createArmorScrap(3);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        if (t4_ore > 0) {
            int payout = t4_ore / 2;
            while (payout > 64) {
                payout -= 64;
                ItemStack scrap = ItemManager.createArmorScrap(4);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
            }
            ItemStack scrap = ItemManager.createArmorScrap(4);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        if (t5_ore > 0) {
            int payout = t5_ore / 2;
            while (payout > 64) {
                payout -= 64;
                ItemStack scrap = ItemManager.createArmorScrap(5);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
            }
            ItemStack scrap = ItemManager.createArmorScrap(5);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        
        
        if (t1_scraps > 0) {
            while (t1_scraps >= 80) {
                t1_scraps -= 80;
                ItemStack scroll = ItemManager.createWeaponEnchant(1);
                merchant_offer.add(scroll);
            }
            while (t1_scraps >= 70) {
                t1_scraps -= 70;
                ItemStack scroll = ItemManager.createArmorEnchant(1);
                merchant_offer.add(scroll);
            }
            int payout = t1_scraps / 2;
            while (payout > 64) {
                ItemStack scrap = ItemManager.createArmorScrap(2);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
                payout -= 64;
            }
            ItemStack scrap = ItemManager.createArmorScrap(2);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        if (t2_scraps > 0) {
        	
            while (t2_scraps >= 140) {
                t2_scraps -= 140;
                ItemStack scroll = ItemManager.createWeaponEnchant(2);
                merchant_offer.add(scroll);
            }

            while (t2_scraps >= 125) {
                t2_scraps -= 125;
                ItemStack scroll = ItemManager.createArmorEnchant(2);
                merchant_offer.add(scroll);
            }
        	
            int payout = 2 * t2_scraps;
            while (payout > 64) {
                ItemStack scrap = ItemManager.createArmorScrap(1);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
                payout -= 64;
            }
            ItemStack scrap = ItemManager.createArmorScrap(1);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        if (t3_scraps > 0) {
            while (t3_scraps >= 110) {
                t3_scraps -= 110;
                ItemStack scroll = ItemManager.createWeaponEnchant(3);
                merchant_offer.add(scroll);
            }

            while (t3_scraps >= 100) {
                t3_scraps -= 100;
                ItemStack scroll = ItemManager.createArmorEnchant(3);
                merchant_offer.add(scroll);
            }
            int payout = 2 * t3_scraps;
            while (payout > 64) {
                ItemStack scrap = ItemManager.createArmorScrap(2);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
                payout -= 64;
            }
            ItemStack scrap = ItemManager.createArmorScrap(2);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        if (t4_scraps > 0) {
            while (t4_scraps >= 88) {
                t4_scraps -= 88;
                ItemStack scroll = ItemManager.createWeaponEnchant(4);
                merchant_offer.add(scroll);
            }

            while (t4_scraps >= 80) {
                t4_scraps -= 80;
                ItemStack scroll = ItemManager.createArmorEnchant(4);
                merchant_offer.add(scroll);
            }

            while (t4_scraps >= 60) {
                t4_scraps -= 60;
                ItemStack orb = ItemManager.createOrbofAlteration();
                merchant_offer.add(orb);
            }
            int payout = 2 * t4_scraps;
            while (payout > 64) {
                ItemStack scrap = ItemManager.createArmorScrap(3);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
                payout -= 64;
            }
            ItemStack scrap = ItemManager.createArmorScrap(3);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        if (t5_scraps > 0) {
            while (t5_scraps >= 33) {
                t5_scraps -= 33;
                ItemStack scroll = ItemManager.createWeaponEnchant(5);
                merchant_offer.add(scroll);
            }

            while (t5_scraps >= 30) {
                t5_scraps -= 30;
                ItemStack scroll = ItemManager.createArmorEnchant(5);
                merchant_offer.add(scroll);
            }

            while (t5_scraps >= 20) {
                t5_scraps -= 20;
                ItemStack orb = ItemManager.createOrbofAlteration();
                merchant_offer.add(orb);
            }
            int payout = 3 * t5_scraps;
            while (payout > 64) {
                ItemStack scrap = ItemManager.createArmorScrap(4);
                scrap.setAmount(64);
                merchant_offer.add(scrap);
                payout -= 64;
            }
            ItemStack scrap = ItemManager.createArmorScrap(4);
            scrap.setAmount(payout);
            merchant_offer.add(scrap);
        }
        //TODO: Trade Enchantment Scrolls for Scraps based on upcoming poll.
        return merchant_offer;
    }
}
