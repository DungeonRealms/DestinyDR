package net.dungeonrealms.old.game.enchantments;

import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Chase on Nov 19, 2015
 */
public class EnchantmentAPI {

    private static Enchantment glowEnchant;

    public static Enchantment getGlowEnchant() {
        if (glowEnchant == null) {
            registerEnchant();
        }
        return glowEnchant;
    }

    public static void addGlow(org.bukkit.inventory.ItemStack stack) {
        stack.addUnsafeEnchantment(getGlowEnchant(), 1);
    }

    public static void removeGlow(ItemStack stack) {
        stack.removeEnchantment(getGlowEnchant());
    }

    public static ItemStack removeItemProtection(ItemStack itemStack) {
        if (!isItemProtected(itemStack))
            return itemStack;
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = meta.getLore();
        lore.remove(ChatColor.GOLD + "Protected");
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsStack.getTag();
        tag.setString("protected", "false");
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static ItemStack addItemProtection(ItemStack itemStack) {
        if (!(isItemProtected(itemStack))) {
            ItemMeta meta = itemStack.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(ChatColor.GOLD + "Protected");
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound tag = nmsStack.getTag();
            tag.setString("protected", "true");
            nmsStack.setTag(tag);
            return CraftItemStack.asBukkitCopy(nmsStack);
        } else {
            return itemStack;
        }
    }

    public static boolean isItemProtected(ItemStack itemStack) {
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        return tag != null && tag.getString("protected").equalsIgnoreCase("true");
    }

    private static void registerEnchant() {
        org.bukkit.enchantments.Enchantment glow = new EnchantGlow(120);
        try {
            /* Adding the new enchant */
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            EnchantmentWrapper.registerEnchantment(glow);
        } catch (IllegalArgumentException ignored) {

        }
        glowEnchant = glow;
    }

    /**
     * @param itemStack
     * @return
     */
    public static int getEnchantLvl(ItemStack itemStack) {
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag != null) {
            return tag.getInt("enchant");
        } else {
            return 0;
        }
    }

}
