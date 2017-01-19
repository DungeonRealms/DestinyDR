package net.dungeonrealms.game.world.loot.types;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.banks.BankMechanics;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Chase on Nov 10, 2015
 */
public enum LootType {
    ArcherTest("archer_test.loot"),
    F1("f1.loot"),
    F2("f2.loot"),
    F3("f3.loot"),
    F4("f4.loot"),
    F5("f5.loot"),
    F6("f6.loot"),
    F7("f7.loot"),
    //	FarmTest("farm_test.loot"),
    M1("m1.loot"),
    M2("m2.loot"),
    M3("m3.loot"),
    M4("m4.loot"),
    M5("m5.loot"),
    NPCChest1("npchest1.loot"),
    NPCChest2("npchest2.loot"),
    NPCChest3("npchest3.loot"),
    NPCChest4("npchest4.loot"),
    NPCChest5("npchest5.loot"),
    NPCChest6("npchest6.loot"),
    NPCChest7("npchest7.loot"),
    NPCChest8("npchest8.loot"),
    NPCChest9("npchest9.loot"),
    NPCChest10("npchest10.loot"),
    NPCChest11("npchest11.loot"),
    NPCChest12("npchest12.loot");
//	PotTest("pot_test.loot"),
//	Test("test.loot"),
//	Tutorial("tutorial.loot");

    public String fileName;
    public HashMap<ItemStack, Double> loot = new HashMap<>();

    LootType(String fileName) {
        this.fileName = fileName;
    }

    public static LootType getLootType(String fileName) {
        for (LootType type : values()) {
            if (type.fileName.equalsIgnoreCase(fileName))
                return type;
        }
        return null;
    }

    public HashMap<ItemStack, Double> getLoot() {
        return loot;
    }

    public static void initLoot() {
        for (LootType type : values()) {
            type.loadItems();
        }
    }

    /**
     *
     */
    private void loadItems() {
        File file = new File(GameAPI.getRemoteDataFolder() + "//loot//" + fileName);
        if (!file.exists()) {
            Utils.log.info(file.getAbsolutePath() + " DOES NOT EXIST");
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = reader.readLine()) != null) {
                if (line.contains(" ")) {
                    line = line.substring(0, line.indexOf(" "));
                    if (line.equalsIgnoreCase(""))
                        continue;
                }
                String s = line;
                String item_id_s = s.substring(0, s.indexOf(":"));
                int item_id = 0;
                int item_tier = -1;
                short item_meta = 0;
                if (item_id_s.contains("T")) {
                    item_id = -1;
                    item_tier = Integer.parseInt(item_id_s.substring(1, item_id_s.length())); // Skip the 'T'.
                } else if (!(item_id_s.contains("T"))) {
                    if (item_id_s.contains(",")) {
                        item_meta = Short.parseShort(item_id_s.split(",")[1]);
                        item_id = Integer.parseInt(item_id_s.split(",")[0]);
                    } else if (!(item_id_s.contains(","))) {
                        item_meta = 0;
                        item_id = Integer.parseInt(item_id_s);
                    }
                }
                double spawn_chance = Double.parseDouble(s.substring(s.indexOf("%") + 1, s.length())) * 10.0D;
                if (spawn_chance < 1) {
                    spawn_chance = 1;
                }
                if (item_id != -1) {
                    Material m = Material.getMaterial(item_id);
                    ItemStack item = null;//new ItemStack(m, 1, item_meta);
                    switch (m) {
                        case FLOWER_POT:
                        case FLOWER_POT_ITEM:
                            //Quiver drops, replace these with gems for now.
                            item = BankMechanics.createBankNote(120);
                            break;
                        case EMERALD:
                            int min_amount = Integer.parseInt(s.substring(s.indexOf(":") + 1, s.indexOf("-")));
                            int max_amount = Integer.parseInt(s.substring(s.indexOf("-") + 1, s.indexOf("%")));
                            int amount_to_spawn = 0;
                            if (max_amount - min_amount > 0) {
                                amount_to_spawn = new Random().nextInt((max_amount - min_amount)) + min_amount;
                            } else if (max_amount - min_amount <= 0) {
                                amount_to_spawn = max_amount; // They're the same value.
                            }

                            if (amount_to_spawn > 64) {
                                item = BankMechanics.createBankNote(amount_to_spawn);
                            } else {
                                item = BankMechanics.gem.clone();
                                item.setAmount(amount_to_spawn);
                            }
                            break;
                        case POTION:
                            switch (item_meta) {
                                case 1:
                                    item = ItemManager.createHealthPotion(1, false, false);
                                    break;
                                case 5:
                                    item = ItemManager.createHealthPotion(2, false, false);
                                    break;
                                case 9:
                                    item = ItemManager.createHealthPotion(3, false, false);
                                    break;
                                case 12:
                                    item = ItemManager.createHealthPotion(4, false, false);
                                    break;
                                case 3:
                                    item = ItemManager.createHealthPotion(5, false, false);
                                    break;
                                case 16385:
                                    item = ItemManager.createHealthPotion(1, false, true);
                                    break;
                                case 16389:
                                    item = ItemManager.createHealthPotion(2, false, true);
                                    break;
                                case 16393:
                                    item = ItemManager.createHealthPotion(3, false, true);
                                    break;
                                case 16396:
                                    item = ItemManager.createHealthPotion(4, false, true);
                                    break;
                                case 16387:
                                    item = ItemManager.createHealthPotion(5, false, true);
                                    break;
                            }
                            break;
                        case EMPTY_MAP:
                            switch (item_meta) {
                                case 1:
                                    item = ItemManager.createRandomTeleportBook();
                                    break;
                                case 2:
                                    item = ItemManager.createRandomTeleportBook();
                                    break;
                                case 3:
                                    item = ItemManager.createRandomTeleportBook();
                                    break;
                                case 4:
                                    item = ItemManager.createRandomTeleportBook();
                                    break;
                                case 11:
                                    item = ItemManager.createWeaponEnchant(1);
                                    break;
                                case 12:
                                    item = ItemManager.createWeaponEnchant(2);
                                    break;
                                case 13:
                                    item = ItemManager.createWeaponEnchant(3);
                                    break;
                                case 14:
                                    item = ItemManager.createWeaponEnchant(4);
                                    break;
                                case 15:
                                    item = ItemManager.createWeaponEnchant(5);
                                    break;
                                case 21:
                                    item = ItemManager.createArmorEnchant(1);
                                    break;
                                case 22:
                                    item = ItemManager.createArmorEnchant(2);
                                    break;
                                case 23:
                                    item = ItemManager.createArmorEnchant(3);
                                    break;
                                case 24:
                                    item = ItemManager.createArmorEnchant(4);
                                    break;
                                case 25:
                                    item = ItemManager.createArmorEnchant(5);
                                    break;
                                default:
                                    continue;
                            }
                            break;
                        case SNOW_BALL:
                        case WATCH:
                            continue;
                        case CHEST:
                            item = ItemManager.createRealmChest();
                            break;
                        case MAGMA_CREAM:
                            item = ItemManager.createOrbofAlteration();
                            break;
                        case MELON_BLOCK:
                            item = new ItemStack(Material.MELON, 16);
                            break;
                        default:
                            item = new ItemStack(m, 1, item_meta);
                            break;
                    }
                    if (item == null) {
                        Utils.log.info("NULL " + item_id + " " + item_meta);
                        continue;
                    }
                    loot.put(item, spawn_chance);
                } else {
                    if (item_tier != -1) {

                        //NO ARMOR AND WEAPONS IN LOOT CHESTS...
//                        ItemStack stack = new ItemStack(Material.IRON_SWORD, 1);
//                        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
//                        nms.setTag(new NBTTagCompound());
//                        if(item_tier >= 5)
//                        	item_tier = 4;
//                        nms.getTag().setInt("itemTier", item_tier);
//                        loot.put(CraftItemStack.asBukkitCopy(nms), spawn_chance);
                    }
                }
            }
            reader.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
