package net.dungeonrealms.game.enchantments;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

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
}
