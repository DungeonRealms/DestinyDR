package net.dungeonrealms.game.world.items.repairing;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.items.Attribute;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

/**
 * Created by Kieran on 9/26/2015.
 */
public class RepairAPI {

    public static int getItemRepairCost(ItemStack i) {
        double repair_cost = 0;
        if (API.isArmor(i)) { // It's a piece of armor.
            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(i);
            if (!nms.hasTag() && nms.getTag() != null && !nms.getTag().hasKey("itemTier")) return -1;
            int item_tier = nms.getTag().getInt("itemTier");
            double maxStat;
            double minStat;
            if (nms.getTag().hasKey("armor")) {
                maxStat = nms.getTag().getInt("armorMax");
                minStat = nms.getTag().getInt("armorMin");
            } else {
                maxStat = nms.getTag().getInt("dpsMax");
                minStat = nms.getTag().getInt("dpsMin");
            }
            double avgStat = (minStat + maxStat) / 2;
            double percent_durability_left = (getCustomDurability(i) / 1500) * 100;  // getDurabilityValueAsPercent(i, getCustomDurability(i));
            if (percent_durability_left > 99) {
                percent_durability_left = 99;
            }
            double armor_cost = avgStat * 1; // This is the cost PER PERCENT

            double global_multiplier = 0.24; // Additional 0.06 less
            double multiplier = 1;
            double missing_percent = 100 - percent_durability_left;
            double total_armor_cost = missing_percent * armor_cost;

            if (item_tier == 1) {
                multiplier = 1.0;
                repair_cost = total_armor_cost * multiplier;
            }
            if (item_tier == 2) {
                multiplier = 1.25;
                repair_cost = total_armor_cost * multiplier;
            }
            if (item_tier == 3) {
                multiplier = 1.5;
                repair_cost = total_armor_cost * multiplier;
            }
            if (item_tier == 4) {
                multiplier = 3.75;
                repair_cost = total_armor_cost * multiplier;
            }
            if (item_tier == 5) {
                multiplier = 6;
                repair_cost = total_armor_cost * multiplier;
            }

            repair_cost = repair_cost * global_multiplier;

        } else if (API.isWeapon(i)) { // It's a weapon.
            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(i);
            if (!nms.hasTag() && nms.getTag() != null && !nms.getTag().hasKey("itemTier")) return -1;
            int item_tier = nms.getTag().getInt("itemTier");
            double maxStat = nms.getTag().getInt("damageMax");
            double minStat = nms.getTag().getInt("damageMin");
            double avgStat = (maxStat + minStat) / 2;
            double dmg_cost = avgStat * 0.1; // This is the cost PER PERCENT

            double percent_durability_left = (getCustomDurability(i) / 1500) * 100;

            if (percent_durability_left > 99) {
                percent_durability_left = 99;
            }

            double global_multiplier = 0.25 - 0.05;
            double multiplier = 1.0; // 100%
            double missing_percent = 100 - percent_durability_left;
            double total_dmg_cost = missing_percent * dmg_cost;
            if (item_tier == 1) {
                multiplier = 1.0;
                repair_cost = total_dmg_cost * multiplier;
            }
            if (item_tier == 2) {
                multiplier = 1.25;
                repair_cost = total_dmg_cost * multiplier;
            }
            if (item_tier == 3) {
                multiplier = 2;
                repair_cost = total_dmg_cost * multiplier;
            }
            if (item_tier == 4) {
                multiplier = 6;
                repair_cost = total_dmg_cost * multiplier;
            }
            if (item_tier == 5) {
                multiplier = 9;
                repair_cost = total_dmg_cost * multiplier;
            }
            repair_cost = repair_cost * global_multiplier;

        } else if (Mining.isDRPickaxe(i)) {
            int item_tier = Mining.getPickTier(i);
            double dmg_cost = Math.pow(Mining.getLvl(i), 2) / 100D; // This is the cost PER PERCENT
            double percent_durability_left = (getCustomDurability(i) / 1500) * 100;
            if (percent_durability_left > 99) {
                percent_durability_left = 99;
            }
            double global_multiplier = 0.8;
            double multiplier = 1.0; // 100%
            double missing_percent = 100 - percent_durability_left;
            double total_dmg_cost = missing_percent * dmg_cost;

            if (item_tier == 1) {
                multiplier = 0.5;
                repair_cost = total_dmg_cost * multiplier;
            }
            if (item_tier == 2) {
                multiplier = 0.75;
                repair_cost = total_dmg_cost * multiplier;
            }
            if (item_tier == 3) {
                multiplier = 1;
                repair_cost = total_dmg_cost * multiplier;
            }
            if (item_tier == 4) {
                multiplier = 2;
                repair_cost = total_dmg_cost * multiplier;
            }
            if (item_tier == 5) {
                multiplier = 3;
                repair_cost = total_dmg_cost * multiplier;
            }
            repair_cost = repair_cost * global_multiplier;
        } else if (Fishing.isDRFishingPole(i)) {
            int item_tier = Fishing.getRodTier(i);
            double dmg_cost = Math.pow(Fishing.getLvl(i), 2) / 100D; // This is the cost PER PERCENT
            double percent_durability_left = (getCustomDurability(i) / 1500) * 100;
            if (percent_durability_left > 99) {
                percent_durability_left = 99;
            }
            double global_multiplier = 0.8;
            double multiplier = 1.0; // 100%
            double missing_percent = 100 - percent_durability_left;
            double total_dmg_cost = missing_percent * dmg_cost;

            if (item_tier == 1) {
                multiplier = 0.5;
                repair_cost = total_dmg_cost * multiplier;
            }
            if (item_tier == 2) {
                multiplier = 0.75;
                repair_cost = total_dmg_cost * multiplier;
            }
            if (item_tier == 3) {
                multiplier = 1.0;
                repair_cost = total_dmg_cost * multiplier;
            }
            if (item_tier == 4) {
                multiplier = 2.0;
                repair_cost = total_dmg_cost * multiplier;
            }
            if (item_tier == 5) {
                multiplier = 3.0;
                repair_cost = total_dmg_cost * multiplier;
            }
            repair_cost = repair_cost * global_multiplier;
        }

        if (repair_cost < 1) {
            repair_cost = 1;
        }

        return (int) Math.round(repair_cost);
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
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return 0;
        if (tag.getInt("itemTier") == 0) return 0;
        double percentDurability = (Math.min(1, (itemStack.getType().getMaxDurability() - itemStack.getDurability())) / Math.min(1, itemStack.getType().getMaxDurability()));
        if (tag.getString("type").equalsIgnoreCase("weapon")) {
            //Get the full durability, not sure why its lowballing.
            return Math.round(percentDurability * (1500 / 15));
        }
        if (tag.getString("type").equalsIgnoreCase("armor")) {
            return Math.round(percentDurability * (1500 / 15));
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
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return 0;
        if (tag.getInt("itemTier") == 0) return 0;

        double durabilityHitsLeft = durability / 1500;
        double percentDurability = itemStack.getType().getMaxDurability() - (itemStack.getType().getMaxDurability() * durabilityHitsLeft);

        if (percentDurability == itemStack.getType().getMaxDurability()) {
            percentDurability = itemStack.getType().getMaxDurability() - 1;
        }
        return Math.round(percentDurability);
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
            net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsItem.getTag();
            if (tag == null) return 0;
            if (tag.getInt("itemTier") == 0) return 0;
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
            } else if (tag.getString("type").equalsIgnoreCase("armor")) {
                durabilityPercent = getItemDurabilityValue(itemStack);
                setCustomItemDurability(itemStack, (durabilityPercent * 15));
                durabilityPercent = durabilityPercent * 15;
            } else {
                durabilityPercent = getItemDurabilityValue(itemStack);
                setCustomItemDurability(itemStack, (durabilityPercent * 15));
                durabilityPercent = durabilityPercent * 15;
            }


            return durabilityPercent;
        } catch (Exception ex) {
            Utils.log.warning("[REPAIR] Item durability was not registered! Registering it now for item " + itemStack.toString());

            net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsItem.getTag();
            if (tag == null) return 0;
            if (tag.getInt("itemTier") == 0) return 0;
            double durabilityPercent = getItemDurabilityValue(itemStack);
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
        if (!(itemStack.getType() == Material.LEATHER || itemStack.getType() == Material.IRON_FENCE || (itemStack.getType() == Material.INK_SACK && (itemStack.getDurability() == DyeColor.YELLOW.getDyeData() || itemStack.getDurability() == DyeColor.LIGHT_BLUE.getDyeData() || itemStack.getDurability() == (short) 7))))
            return false;
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
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
        if (!isItemArmorScrap(itemStack)) return 0;
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
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
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItem == null) {
            return 0;
        }
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return 0;
        if (!tag.hasKey("itemTier")) return 0;
        if (tag.getInt("itemTier") == 0) return 0;
        return tag.getInt("itemTier");
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

        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItem == null) {
            return false;
        }
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return false;
        if (tag.getInt("itemTier") == 0) return false;
        if (tag.getString("type").equalsIgnoreCase("weapon")) {
            return true;
        }
        return tag.getString("type").equalsIgnoreCase("armor");
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
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItem == null) {
            return false;
        }
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return false;
        if (Mining.isDRPickaxe(itemStack) || Fishing.isDRFishingPole(itemStack)) {
            if (getCustomDurability(itemStack) < 1500)
                return true;
        }
        if (tag.getInt("itemTier") == 0) return false;
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

            if (EnchantmentAPI.getEnchantLvl(itemStack) > 3) {
                EnchantmentAPI.addGlow(itemStack);
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
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return;
        if (tag.getInt("itemTier") == 0) return; //Broken tier item.
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
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) return;
        if (tag.getInt("itemTier") == 0) return;
        double newItemDurability = (getCustomDurability(itemStack) - amountToSubtract);
        switch (tag.getString("type")) {
            case "weapon":
                if (newItemDurability <= 150D && newItemDurability >= 140D) {
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + " **10% DURABILITY " + ChatColor.RED + "Left on " + itemStack.getItemMeta().getDisplayName() + "*");
                }
                if (newItemDurability <= 30D && newItemDurability >= 20D) {
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + " **2% DURABILITY " + ChatColor.RED + "Left on " + itemStack.getItemMeta().getDisplayName() + "*");
                }
                if (newItemDurability <= 1D) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        player.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
                        player.updateInventory();
                    }, 10L);
                }
                break;
            case "armor":
                if (newItemDurability <= 150D && newItemDurability >= 140D) {
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + " **10% DURABILITY " + ChatColor.RED + "Left on " + itemStack.getItemMeta().getDisplayName() + "*");
                }
                if (newItemDurability <= 30D && newItemDurability >= 20D) {
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + " **2% DURABILITY " + ChatColor.RED + "Left on " + itemStack.getItemMeta().getDisplayName() + "*");
                }
                if (newItemDurability <= 1D) {
                    switch (new Attribute(itemStack).getItemType().getId()) {
                        case 5:
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                player.getInventory().setHelmet(new ItemStack(Material.AIR));
                                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
                                player.updateInventory();
                            }, 10L);
                            break;
                        case 6:
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                player.getInventory().setChestplate(new ItemStack(Material.AIR));
                                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
                                player.updateInventory();
                            }, 10L);
                            break;
                        case 7:
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                player.getInventory().setLeggings(new ItemStack(Material.AIR));
                                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
                                player.updateInventory();
                            }, 10L);
                            break;
                        case 8:
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                player.getInventory().setBoots(new ItemStack(Material.AIR));
                                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
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
    }
}
