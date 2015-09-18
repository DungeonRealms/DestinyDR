/**
 * 
 */
package net.dungeonrealms.banks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumOperators;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagDouble;
import net.minecraft.server.v1_8_R3.NBTTagString;

/**
 * Created by Chase on Sep 18, 2015
 */
public class BankMechanics {

	public static ItemStack gem;

	public void init() {
		gem = loadCurrency();
	}

	/**
	 * @return
	 */
	private ItemStack loadCurrency() {
		ItemStack item = new ItemStack(Material.EMERALD, 1);
		ItemMeta meta = gem.getItemMeta();
		List<String> lore = new ArrayList<String>();
		lore.add("The currency of Andalucia");
		meta.setLore(lore);
		meta.setDisplayName("Gem");
		gem.setItemMeta(meta);
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(gem);
		NBTTagCompound tag = nms.getTag() == null ? new NBTTagCompound() : nms.getTag();
		tag.setBoolean("money", true);
		nms.setTag(tag);
		return CraftItemStack.asBukkitCopy(nms);
	}

	/**
	 * @param player
	 */
	public static void addGemsToPlayer(Player player, int num) {
		DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, "gems", num);
	}

}
