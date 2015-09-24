package net.dungeonrealms.items.enchanting;

import net.dungeonrealms.items.Attribute;
import net.dungeonrealms.items.Item;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Created by Kieran on 9/20/2015.
 */
class EnchantmentAPI {

    /**
     * Checks the item to see if its a protection scroll
     * @param itemStack
     * @since 1.0
     */
    public static boolean isProtectionScroll(ItemStack itemStack) {
        if (itemStack.getType() != Material.EMPTY_MAP) {
            return false;
        }
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        return !(tag == null || nmsItem == null) && tag.getString("type").equalsIgnoreCase("protection");
    }

    /**
     * Checks the item to see if the scroll is for the correct item (weapon/armor)
     * @param scroll
     * @param itemToProtect
     * @since 1.0
     */
    public static boolean isCorrectProtectionScroll(ItemStack scroll, ItemStack itemToProtect) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(scroll);
        NBTTagCompound tag = nmsItem.getTag();
        return tag.getString("usage").equalsIgnoreCase("weapon") && isItemWeapon(itemToProtect) && tag.getString("usage").equalsIgnoreCase("armor")
                && isItemArmor(itemToProtect) && doItemTiersMatch(scroll, itemToProtect);
    }

    /**
     * Checks the item to see if its a weapon
     * @param itemStack
     * @since 1.0
     */
    private static boolean isItemWeapon(ItemStack itemStack) {
        Item.ItemType itemType = new Attribute(itemStack).getItemType();
        return itemType == Item.ItemType.AXE || itemType == Item.ItemType.POLE_ARM || itemType == Item.ItemType.SWORD;
    }

    /**
     * Checks the item to see if its armor
     * @param itemStack
     * @since 1.0
     */
    private static boolean isItemArmor(ItemStack itemStack) {
        Item.ItemType itemType = new Attribute(itemStack).getItemType();
        //TODO: CHECK WHEN ARMORTYPE IS ADDED
        return false;
    }

    /**
     * Checks the items to see if the tiers match
     * @param itemStack
     * @param toCompare
     * @since 1.0
     */
    private static boolean doItemTiersMatch(ItemStack itemStack, ItemStack toCompare) {
        return new Attribute(itemStack).getItemTier() == new Attribute(toCompare).getItemTier();
    }

    /**
     * Checks the item to see if its already protected
     * @param itemStack
     * @since 1.0
     */
    public static boolean isItemProtected(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        return !tag.getString("protected").equalsIgnoreCase("false");
    }

    /**
     * Removes the items protection (have to check if its protected before doing this!)
     * @param itemStack
     * @since 1.0
     */
    public static ItemStack removeItemProtection(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = meta.getLore();
        lore.remove("PROTECTED");
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsStack.getTag();
        tag.set("protected", new NBTTagString("false"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    /**
     * Adds protection to the item (have to check that its not protected before doing this!)
     * @param itemStack
     * @since 1.0
     */
    public static ItemStack addItemProtection(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = meta.getLore();
        lore.add("PROTECTED");
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsStack.getTag();
        tag.set("protected", new NBTTagString("true"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }
}
