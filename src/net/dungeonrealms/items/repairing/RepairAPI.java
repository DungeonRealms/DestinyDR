package net.dungeonrealms.items.repairing;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.items.Attribute;
import net.dungeonrealms.items.enchanting.EnchantmentAPI;
import net.dungeonrealms.mechanics.SoundAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

/**
 * Created by Kieran on 9/26/2015.
 */
public class RepairAPI {

    public int getItemRepairCost(ItemStack itemStack) {
        double totalRepairCost = 0;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return 0 ;
        if (tag.getInt("itemTier") == 0) return 0; //Broken tier item.
        if (tag.getString("type").equalsIgnoreCase("weapon")) {
            double damagePercentCost = tag.getInt("damage") * 0.1;
            double weaponDurabilityLeft = getItemDurabilityValue(itemStack);
            if (weaponDurabilityLeft > 99) {
                weaponDurabilityLeft = 99;
            }
            totalRepairCost = ((100 - weaponDurabilityLeft) * damagePercentCost);
            switch (tag.getInt("itemTier")) {
                case 1:
                    totalRepairCost *= 1.05;
                    break;
                case 2:
                    totalRepairCost *= 1.20;
                    break;
                case 3:
                    totalRepairCost *= 1.9;
                    break;
                case 4:
                    totalRepairCost *= 5.5;
                    break;
                case 5:
                    totalRepairCost *= 8.0;
                    break;
                default:
                    totalRepairCost *= 4;
                    break;
            }
            totalRepairCost *= 0.2;
        }
        if (tag.getString("type").equalsIgnoreCase("armor")) {
            double armorPercentCost =  tag.getInt("armor") * 0.4;
            double armorDurabilityLeft = getItemDurabilityValue(itemStack);
            if (armorDurabilityLeft > 99) {
                armorDurabilityLeft = 99;
            }
            totalRepairCost = ((100 - armorDurabilityLeft) * armorPercentCost);
            switch (tag.getInt("itemTier")) {
                case 1:
                    totalRepairCost *= 1.05;
                    break;
                case 2:
                    totalRepairCost *= 1.20;
                    break;
                case 3:
                    totalRepairCost *= 1.45;
                    break;
                case 4:
                    totalRepairCost *= 3.5;
                    break;
                case 5:
                    totalRepairCost *= 5.5;
                    break;
                default:
                    totalRepairCost *= 2;
                    break;
            }
            totalRepairCost *= 0.25;
        }

        if (totalRepairCost < 1) {
            totalRepairCost = 1;
        }

        return (int) Math.round(totalRepairCost);
    }

    public static double getItemDurabilityValue(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return 0 ;
        if (tag.getInt("itemTier") == 0) return 0; //Broken tier item.
        double percentDurability = ((itemStack.getType().getMaxDurability() - itemStack.getDurability()) / itemStack.getType().getMaxDurability()); //Weird DR formula?
        if (tag.getString("type").equalsIgnoreCase("weapon")) {
            return Math.round(percentDurability * (1450 / 15));
        }
        if (tag.getString("type").equalsIgnoreCase("armor")) {
            return Math.round(percentDurability * (1550 / 15));
        }
        return 0;
    }

    public static boolean isItemArmorScrap(ItemStack itemStack) {
        if (!(itemStack.getType() == Material.LEATHER || itemStack.getType() == Material.IRON_FENCE || itemStack.getType() == Material.INK_SACK))
            return false;
        if (itemStack.getType() == Material.INK_SACK) {
            if (itemStack.getDurability() != 7 && itemStack.getDurability() != 11 && itemStack.getDurability() != 12)
                return false;
        }
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        return tag != null && tag.getString("type").equalsIgnoreCase("scrap");
    }

    public static void setCustomItemDurability(ItemStack itemStack, int durability) {
        try {
            Repairable repairable = (Repairable) itemStack.getItemMeta();
            repairable.setRepairCost(durability);
            itemStack.setItemMeta((ItemMeta) repairable);

            if (EnchantmentAPI.getItemEnchantmentLevel(itemStack) >= 4) {
                EnchantmentAPI.addCustomEnchantToItem(itemStack);
            }

            setPercentageDurabilityBar(itemStack, durability);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setPercentageDurabilityBar(ItemStack itemStack, int percent) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return;
        if (tag.getInt("itemTier") == 0) return; //Broken tier item.
        double newDurability = getItemDurabilityValue(itemStack);
        if (newDurability < 1 && percent < 99) {
            newDurability = 1;
        }
        itemStack.setDurability((short) newDurability);
    }

    public static void subtractCustomDurability(Player player, ItemStack itemStack, double amountToSubtract) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return;
        if (tag.getInt("itemTier") == 0) return; //Broken tier item.
        double newItemDurability = (getItemDurabilityValue(itemStack) - amountToSubtract);
        switch (tag.getString("type")) {
            case "weapon":
                if (newItemDurability <= 100D && newItemDurability >= 90D) {
                    SoundAPI.getInstance().playSound("random.anvil_break", player);
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + " **10% DURABILITY " + ChatColor.RED + "Left on " + itemStack.getItemMeta().getDisplayName() + "*");
                }
                if (newItemDurability <= 20D && newItemDurability >= 10D) {
                    SoundAPI.getInstance().playSound("random.anvil_break", player);
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + " **2% DURABILITY " + ChatColor.RED + "Left on " + itemStack.getItemMeta().getDisplayName() + "*");
                }
                if (newItemDurability <= 0.1D) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        player.setItemInHand(new ItemStack(Material.AIR));
                        SoundAPI.getInstance().playSound("random.anvil_break", player);
                        player.updateInventory();
                    }, 10L);
                }
                break;
            case "armor":
                if (newItemDurability <= 150D && newItemDurability >= 140D) {
                    SoundAPI.getInstance().playSound("random.anvil_break", player);
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + " **10% DURABILITY " + ChatColor.RED + "Left on " + itemStack.getItemMeta().getDisplayName() + "*");
                }
                if (newItemDurability <= 30D && newItemDurability >= 20D) {
                    SoundAPI.getInstance().playSound("random.anvil_break", player);
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + " **2% DURABILITY " + ChatColor.RED + "Left on " + itemStack.getItemMeta().getDisplayName() + "*");
                }
                if (newItemDurability <= 0.1D) {
                    switch (new Attribute(itemStack).getArmorType().getId()) {
                        case 0:
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                player.getInventory().setHelmet(new ItemStack(Material.AIR));
                                SoundAPI.getInstance().playSound("random.anvil_break", player);
                                player.updateInventory();
                            }, 10L);
                            break;
                        case 1:
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                player.getInventory().setChestplate(new ItemStack(Material.AIR));
                                SoundAPI.getInstance().playSound("random.anvil_break", player);
                                player.updateInventory();
                            }, 10L);
                            break;
                        case 2:
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                player.getInventory().setLeggings(new ItemStack(Material.AIR));
                                SoundAPI.getInstance().playSound("random.anvil_break", player);
                                player.updateInventory();
                            }, 10L);
                            break;
                        case 3:
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                player.getInventory().setBoots(new ItemStack(Material.AIR));
                                SoundAPI.getInstance().playSound("random.anvil_break", player);
                                player.updateInventory();
                            }, 10L);
                            break;
                        default:
                            break;
                    }
                    //TODO : PROFESSION ITEMS WITH DIFFERENT SYSTEM.
                    //TODO : CHECK PLAYERS HP AND REGEN THEN REDUCE AFTER ARMOR BREAKING.
                }
                break;
            default:
                break;
        }
        if (newItemDurability > 0.1D) {
            setCustomItemDurability(itemStack, (int) newItemDurability);
        }

        player.updateInventory();
    }
}
