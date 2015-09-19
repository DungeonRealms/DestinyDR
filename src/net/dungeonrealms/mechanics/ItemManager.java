package net.dungeonrealms.mechanics;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

/**
 * Created by Nick on 9/18/2015.
 */
public class ItemManager {
	/**
	 * returns hearthstone
	 * 
	 * @param name
	 * @param lore
	 * @return
	 * @since 1.0
	 */
	public static ItemStack getHearthStone(String name, String[] lore) {
		ItemStack rawStack = new ItemStack(Material.QUARTZ);
		ItemMeta meta = rawStack.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(Arrays.asList(lore));
		rawStack.setItemMeta(meta);
		net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(rawStack);
		NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
		tag.set("type", new NBTTagString("important"));
		tag.set("usage", new NBTTagString("hearthstone"));
		nmsStack.setTag(tag);
		return CraftItemStack.asBukkitCopy(nmsStack);
	}

	/**
	 * 
	 * @param material
	 * @param name
	 * @param lore
	 * @return
	 */
	public static ItemStack createItem(Material m, String name, String[] lore) {
		ItemStack is = new ItemStack(m, 1);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName(name);
		if (lore != null)
			meta.setLore(Arrays.asList(lore));
		is.setItemMeta(meta);
		return is;
	}
	public static ItemStack createItemWithData(Material m, String name, String[] lore, byte b) {
		ItemStack is = new ItemStack(m, 1, b);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName(name);
		if (lore != null)
			meta.setLore(Arrays.asList(lore));
		is.setItemMeta(meta);
		return is;
	}
	/**
	 * returns playerProfile
	 * 
	 * @param player
	 * @param displayName
	 * @param lore
	 * @return
	 * @since 1.0
	 */
	public static ItemStack getPlayerProfile(Player player, String displayName, String[] lore) {
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwner(player.getName());
		meta.setDisplayName(displayName);
		meta.setLore(Arrays.asList(lore));
		skull.setItemMeta(meta);
		net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(skull);
		NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
		tag.set("type", new NBTTagString("important"));
		tag.set("usage", new NBTTagString("profile"));
		nmsStack.setTag(tag);
		return CraftItemStack.asBukkitCopy(nmsStack);
	}
}
