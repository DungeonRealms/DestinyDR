package net.dungeonrealms.enchantments;

import java.lang.reflect.Field;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;

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
		} catch (IllegalArgumentException e) {

		}
		glowEnchant = glow;
	}

}
