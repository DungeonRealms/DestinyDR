package net.dungeonrealms.items.repairing;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.items.Attribute;
import net.dungeonrealms.items.enchanting.EnchantmentAPI;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.SoundAPI;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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

    /**
     * Returns the repair cost
     * of a specified itemstack
     *
     * @param itemStack
     * @return int
     * @since 1.0
     */
    public int getItemRepairCost(ItemStack itemStack) {
        double totalRepairCost = 0;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return 0;
        if (tag.getInt("itemTier") == 0 && tag.getInt("armorTier") == 0) return 0;
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
            double armorPercentCost = tag.getInt("armor") * 0.4;
            double armorDurabilityLeft = getItemDurabilityValue(itemStack);
            if (armorDurabilityLeft > 99) {
                armorDurabilityLeft = 99;
            }
            totalRepairCost = ((100 - armorDurabilityLeft) * armorPercentCost);
            switch (tag.getInt("armorTier")) {
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

    /**
     * Returns the base durability
     * of a specified itemstack
     *
     * @param itemStack
     * @return double
     * @since 1.0
     */
    public static double getItemDurabilityValue(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return 0;
        if (tag.getInt("itemTier") == 0 && tag.getInt("armorTier") == 0) return 0;
        double percentDurability = ((itemStack.getType().getMaxDurability() - itemStack.getDurability()) / itemStack.getType().getMaxDurability());
        if (tag.getString("type").equalsIgnoreCase("weapon")) {
            return Math.round(percentDurability * (1450 / 15));
        }
        if (tag.getString("type").equalsIgnoreCase("armor")) {
            return Math.round(percentDurability * (1550 / 15));
        }
        return 0;
    }

    /**
     * Returns the durability percentage
     * of a specified itemstack
     *
     * @param itemStack
     * @return double
     * @since 1.0
     */
    public static double getDurabilityValueAsPercent(ItemStack itemStack, double durability) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return 0;
        if (tag.getInt("itemTier") == 0 && tag.getInt("armorTier") == 0) return 0;
        double durabilityHitsLeft = durability / 1500;
        double percentDurability = 1500 - (1500 * durabilityHitsLeft);
        if (percentDurability == 1500) {
            percentDurability = 1500 - 1;
        }
        return (double) Math.round(percentDurability);
    }

    /**
     * Returns the current durability
     * of a specified itemstack or
     * sets it if it does not have one
     *
     * @param itemStack
     * @return double
     * @since 1.0
     */
    public static double getCustomDurability(ItemStack itemStack) {
        try {
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsItem.getTag();
            if (tag == null) return 0;
            if (tag.getInt("itemTier") == 0 && tag.getInt("armorTier") == 0) return 0;
            Repairable repairable = (Repairable) itemStack.getItemMeta();
            double durability = repairable.getRepairCost();

            if (durability > 0) {
                return durability;
            }
            if (durability < 0) {
                return 0;
            }
            double durabilityPercent = 0;
            if (tag.getString("type").equalsIgnoreCase("weapon")) {
                durabilityPercent = getItemDurabilityValue(itemStack);
                setCustomItemDurability(itemStack, (durabilityPercent * 15));
                durabilityPercent = durabilityPercent * 15;
            }
            if (tag.getString("type").equalsIgnoreCase("armor")) {
                durabilityPercent = getItemDurabilityValue(itemStack);
                setCustomItemDurability(itemStack, (durabilityPercent * 15));
                durabilityPercent = durabilityPercent * 15;
            }

            return durabilityPercent;
        } catch (Exception ex) {
            Utils.log.warning("[REPAIR] Item durability was not registered! Registering it now for item " + itemStack.toString());

            net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsItem.getTag();
            if (tag == null) return 0;
            if (tag.getInt("itemTier") == 0 && tag.getInt("armorTier") == 0) return 0;
            double durabilityPercent =  getItemDurabilityValue(itemStack);
            if (tag.getString("type").equalsIgnoreCase("weapon")) {
                durabilityPercent = getItemDurabilityValue(itemStack);
                setCustomItemDurability(itemStack, (durabilityPercent * 15));
                durabilityPercent = durabilityPercent * 15;
            }
            if (tag.getString("type").equalsIgnoreCase("armor")) {
                durabilityPercent = getItemDurabilityValue(itemStack);
                setCustomItemDurability(itemStack, (durabilityPercent * 15));
                durabilityPercent = durabilityPercent * 15;
            }

            return durabilityPercent;
        }
    }

    /**
     * Checks if the itemstack
     * is an armor scrap
     * (Used to repair armor)
     *
     * @param itemStack
     * @return boolean
     * @since 1.0
     */
    public static boolean isItemArmorScrap(ItemStack itemStack) {
        if (!(itemStack.getType() == Material.LEATHER || itemStack.getType() == Material.IRON_FENCE || itemStack.getType() == Material.IRON_INGOT || itemStack.getType() == Material.DIAMOND || itemStack.getType() == Material.GOLD_INGOT))
            return false;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItem == null) {
            return false;
        }
        NBTTagCompound tag = nmsItem.getTag();
        return tag != null && tag.getString("type").equalsIgnoreCase("scrap");
    }

    /**
     * Returns the item tier of
     * an itemstack that is
     * scrap
     *
     * @param itemStack
     * @return boolean
     * @since 1.0
     */
    public static int getScrapTier(ItemStack itemStack) {
        if (!(itemStack.getType() == Material.LEATHER || itemStack.getType() == Material.IRON_FENCE || itemStack.getType() == Material.IRON_INGOT || itemStack.getType() == Material.DIAMOND || itemStack.getType() == Material.GOLD_INGOT))
            return 0;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItem == null) {
            return 0;
        }
        NBTTagCompound tag = nmsItem.getTag();
        if (tag != null && tag.getString("type").equalsIgnoreCase("scrap")) {
            if (tag.getInt("itemTier") != 0) {
                return tag.getInt("itemTier");
            }
        }
        return 0;
    }

    /**
     * Returns the item tier of
     * an itemstack that is
     * an armor/weapon piece
     *
     * @param itemStack
     * @return boolean
     * @since 1.0
     */
    public static int getArmorOrWeaponTier(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItem == null) {
            return 0;
        }
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return 0;
        if (tag.getInt("itemTier") == 0 && tag.getInt("armorTier") == 0) return 0;
        if (tag.getString("type").equalsIgnoreCase("weapon")) {
            return tag.getInt("itemTier");
        }
        if (tag.getString("type").equalsIgnoreCase("armor")) {
            return tag.getInt("armorTier");
        }
        return 0;
    }

    /**
     * Checks if the itemstack
     * is an armor piece or weapons
     *
     * @param itemStack
     * @return boolean
     * @since 1.0
     */
    public static boolean isItemArmorOrWeapon(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItem == null) {
            return false;
        }
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return false;
        if (tag.getInt("itemTier") == 0 && tag.getInt("armorTier") == 0) return false;
        if (tag.getString("type").equalsIgnoreCase("weapon")) {
            return true;
        }
        if (tag.getString("type").equalsIgnoreCase("armor")) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the itemstack
     * can be repaired
     *
     * @param itemStack
     * @return boolean
     * @since 1.0
     */
    public static boolean canItemBeRepaired(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItem == null) {
            return false;
        }
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return false;
        if (tag.getInt("itemTier") == 0 && tag.getInt("armorTier") == 0) return false;
        if (tag.getString("type").equalsIgnoreCase("weapon") || tag.getString("type").equalsIgnoreCase("armor")) {
            if (getCustomDurability(itemStack) < 1500) {
                return true;
            }
        }
        return false;
    }
    /**
     * Sets the custom durability
     * of a specified itemstack
     * and updates its enchantment
     * if applicable
     *
     * @param itemStack
     * @param durability
     * @since 1.0
     */
    public static void setCustomItemDurability(ItemStack itemStack, double durability) {
        try {
            Repairable repairable = (Repairable) itemStack.getItemMeta();
            repairable.setRepairCost((int) durability);
            itemStack.setItemMeta((ItemMeta) repairable);

            if (EnchantmentAPI.getItemEnchantmentLevel(itemStack) >= 4) {
                EnchantmentAPI.addCustomEnchantToItem(itemStack);
            }

            setPercentageDurabilityBar(itemStack, durability);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Sets the custom durability
     * bar of a specified itemstack
     *
     * @param itemStack
     * @param percent
     * @since 1.0
     */
    public static void setPercentageDurabilityBar(ItemStack itemStack, double percent) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return;
        if (tag.getInt("itemTier") == 0 && tag.getInt("armorTier") == 0) return; //Broken tier item.
        double newDurability = getDurabilityValueAsPercent(itemStack, percent);
        if (newDurability < 1 && percent < 99) {
            newDurability = 1;
        }
        itemStack.setDurability((short) newDurability);
    }

    /**
     * Subtracts durability from
     * a specified itemstack and
     * informs the player if it
     * drops below certain values
     * also handles breaking of item
     *
     * @param player
     * @param itemStack
     * @param amountToSubtract
     * @since 1.0
     */
    public static void subtractCustomDurability(Player player, ItemStack itemStack, double amountToSubtract) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return;
        if (tag.getInt("itemTier") == 0 && tag.getInt("armorTier") == 0) return;
        double newItemDurability = (getCustomDurability(itemStack) - amountToSubtract);
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
            setCustomItemDurability(itemStack, newItemDurability);
        }

        player.updateInventory();
    }
}
