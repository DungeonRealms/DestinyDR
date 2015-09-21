/**
 * 
 */
package net.dungeonrealms.banks;

import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumOperators;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Chase on Sep 18, 2015
 */
public class BankMechanics {

	public static ItemStack gem;

	public static void init() {
		gem = loadCurrency();
	}

	/**
	 * @return
	 */
	private static ItemStack loadCurrency() {
		ItemStack item = new ItemStack(Material.EMERALD, 1);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<String>();
		lore.add("The currency of Andalucia");
		meta.setLore(lore);
		meta.setDisplayName("Gem");
		item.setItemMeta(meta);
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = nms.getTag() == null ? new NBTTagCompound() : nms.getTag();
		tag.setString("type", "money");
		nms.setTag(tag);
		return CraftItemStack.asBukkitCopy(nms);
	}

	/**
	 * @param uuid
	 */
	public static void addGemsToPlayer(UUID uuid, int num) {
		DatabaseAPI.getInstance().update(uuid, EnumOperators.$INC, "info.gems", num);
	}

}
