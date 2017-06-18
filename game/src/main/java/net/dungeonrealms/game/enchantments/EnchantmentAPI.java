package net.dungeonrealms.game.enchantments;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;

public class EnchantmentAPI {

    private static Enchantment glowEnchant;

    public static Enchantment getGlowEnchant() {
        if (glowEnchant == null)
            registerEnchant();
        return glowEnchant;
    }

    public static ItemStack addGlow(org.bukkit.inventory.ItemStack stack) {
        stack.addUnsafeEnchantment(Enchantment.DAMAGE_UNDEAD, 1);
        ItemMeta im = stack.getItemMeta();
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(im);
        return stack;
    }

    public static void removeGlow(ItemStack stack) {
        stack.removeEnchantment(getGlowEnchant());
    }

    public static boolean isGlowing(ItemStack item) {
        return item.getEnchantments().containsKey(getGlowEnchant());
    }

    private static void registerEnchant() {
        org.bukkit.enchantments.Enchantment glow = new EnchantGlow(120);
        try {
            /* Adding the new enchant */
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            EnchantmentWrapper.registerEnchantment(glow);
        } catch (Exception e) {
            e.printStackTrace();
        }
        glowEnchant = glow;
    }
}
