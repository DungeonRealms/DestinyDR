package net.dungeonrealms.items.enchanting;

import net.dungeonrealms.items.Attribute;
import net.dungeonrealms.items.Item;
import net.dungeonrealms.items.armor.Armor;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Kieran on 9/20/2015.
 */
public class EnchantmentAPI implements GenericMechanic {

    private static EnchantmentAPI instance = null;
    static Enchantment enchantment = null;

    public static EnchantmentAPI getInstance() {
        if (instance == null) {
            return new EnchantmentAPI();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.BISHOPS;
    }

    public void startInitialization() {
        registerCustomEnchantment();
    }

    @Override
    public void stopInvocation() {

    }

    /**
     * Returns our custom enchantment, registers it
     * if it isn't already
     *
     * @since 1.0
     */
    public static Enchantment getEnchantment() {
        if (enchantment == null) {
            registerCustomEnchantment();
        }
        return enchantment;
    }

    /**
     * Registers our custom enchantment to act as
     * a Bukkit enchantment
     *
     * @since 1.0
     */
    public static void registerCustomEnchantment() {
        FakeEnchant fakeEnchant = new FakeEnchant(121);
        try {
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.set(null, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            EnchantmentWrapper.registerEnchantment(fakeEnchant);
        } catch (IllegalArgumentException iaex) {
            Utils.log.info("Could not register our custom enchant. Uh oh.");
        }
        enchantment = fakeEnchant;
    }

    /**
     * Adds our custom enchantment to the specified item
     *
     * @param itemStack
     * @since 1.0
     */
    public static void addCustomEnchantToItem(ItemStack itemStack) {
        itemStack.addUnsafeEnchantment(getEnchantment(), 1);
    }

    /**
     * Checks the item to see if its a protection scroll
     *
     * @param itemStack
     * @return boolean
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
     *
     * @param scroll
     * @param itemToProtect
     * @return boolean
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
     *
     * @param itemStack
     * @return boolean
     * @since 1.0
     */
    public static boolean isItemWeapon(ItemStack itemStack) {
        Item.ItemType itemType = new Attribute(itemStack).getItemType();
        return itemType == Item.ItemType.AXE || itemType == Item.ItemType.POLE_ARM || itemType == Item.ItemType.SWORD || itemType == Item.ItemType.STAFF || itemType == Item.ItemType.BOW;
    }

    /**
     * Checks the item to see if its armor
     *
     * @param itemStack
     * @return boolean
     * @since 1.0
     */
    public static boolean isItemArmor(ItemStack itemStack) {
        Armor.EquipmentType armorType = new Attribute(itemStack).getArmorType();
        return armorType == Armor.EquipmentType.BOOTS || armorType == Armor.EquipmentType.LEGGINGS || armorType == Armor.EquipmentType.HELMET || armorType == Armor.EquipmentType.CHESTPLATE;
    }

    /**
     * Checks the items to see if the tiers match
     *
     * @param itemStack
     * @param toCompare
     * @return boolean
     * @since 1.0
     */
    private static boolean doItemTiersMatch(ItemStack itemStack, ItemStack toCompare) {
        return new Attribute(itemStack).getItemTier() == new Attribute(toCompare).getItemTier();
    }


    /**
     * Checks the item to see if its already protected
     *
     * @param itemStack
     * @return boolean
     * @since 1.0
     */
    public static boolean isItemProtected(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        return Boolean.valueOf(String.valueOf(tag.getString("protected")).toLowerCase());
    }

    /**
     * Removes the items protection
     *
     * @param itemStack
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack removeItemProtection(ItemStack itemStack) {
        if (!isItemProtected(itemStack)) return itemStack;
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = meta.getLore();
        lore.remove(ChatColor.GOLD + "Protected");
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsStack.getTag();
        tag.set("protected", new NBTTagString("false"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    /**
     * Adds protection to the item
     *
     * @param itemStack
     * @since 1.0
     */

    public static ItemStack addItemProtection(ItemStack itemStack) {
        if (!(isItemProtected(itemStack))) {
            ItemMeta meta = itemStack.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(ChatColor.GOLD + "Protected");
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag();
            tag.set("protected", new NBTTagString("true"));
            nmsStack.setTag(tag);
            return CraftItemStack.asBukkitCopy(nmsStack);
        } else {
            return itemStack;
        }
    }

    /**
     * Checks the item to see how many enchant levels
     * it currently has
     *
     * @param itemStack
     * @since 1.0
     */
    public static int getItemEnchantmentLevel(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag.getInt("enchantment") == 0) {
            return 0;
        } else {
            return tag.getInt("enchantment");
        }
    }

    public ItemStack enchantItem(ItemStack itemStack, int enchantmentLevel) {
        //TODO:
        //If enchant level is above 3 and it fails. Item will be destroyed, if not then you will only lose enchantment scroll.
        //WEAPONS 5% DAMAGE Increase.
        //ARMOR 5% HP Increase + (5% HP REGEN OR 1% ENERGY REGEN)
        if (isItemArmor(itemStack)) {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            //Enchant Item
            return CraftItemStack.asBukkitCopy(nmsStack);
        }
        if (isItemWeapon(itemStack)) {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            //Enchant Armor
            return CraftItemStack.asBukkitCopy(nmsStack);
        }
        return itemStack;
    }
}
