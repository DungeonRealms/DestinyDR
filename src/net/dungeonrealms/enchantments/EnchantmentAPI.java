package net.dungeonrealms.enchantments;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;

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

	public static ItemStack removeItemProtection(ItemStack itemStack) {
		if (!isItemProtected(itemStack))
			return itemStack;
		ItemMeta meta = itemStack.getItemMeta();
		List<String> lore = meta.getLore();
		lore.remove(ChatColor.GOLD + "Protected");
		meta.setLore(lore);
		itemStack.setItemMeta(meta);
		net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
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
			net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
			NBTTagCompound tag = nmsStack.getTag();
			tag.setString("protected", "true");
			nmsStack.setTag(tag);
			return CraftItemStack.asBukkitCopy(nmsStack);
		} else {
			return itemStack;
		}
	}

	public static boolean isItemProtected(ItemStack itemStack) {
		net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
		NBTTagCompound tag = nmsItem.getTag();
		return tag.getString("protected").equalsIgnoreCase("true");
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

	/**
	 * @param itemStack
	 * @return
	 */
	public static int getEnchantLvl(ItemStack itemStack) {
		net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
		NBTTagCompound tag = nmsItem.getTag();
		return tag.getInt("enchant");
	}

}
