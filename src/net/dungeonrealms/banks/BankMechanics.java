/**
 * 
 */
package net.dungeonrealms.banks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.dungeonrealms.mastery.ItemSerialization;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumOperators;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * Created by Chase on Sep 18, 2015
 */
public class BankMechanics {

	public static ItemStack gem;
	public static ItemStack banknote;
	public static HashMap<UUID, Storage> storage = new HashMap<UUID, Storage>();

	public static void init() {
		loadCurrency();
	}

	
	public static void handleLogout(UUID uuid ){
		if(storage.containsKey(uuid)){
			Inventory inv = storage.get(uuid).inv;
			try {
			String serializedInv = ItemSerialization.serialize(Arrays.asList(inv.getContents()));
			storage.remove(uuid);
			//TODO Update MONGO with @serializedInv string.
			} catch (IOException e) {
			e.printStackTrace();
			}
		}
	}
	
	public static void handleLogin(UUID uuid){
		String serializedInv = "get from mongo"; 
		try {
			List<ItemStack> contents = ItemSerialization.deserialize(serializedInv);
			Storage storageTemp = new Storage(uuid, (ItemStack[]) contents.toArray());
			storage.put(uuid, storageTemp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return
	 */
	private static void loadCurrency() {
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
		gem = CraftItemStack.asBukkitCopy(nms);

		ItemStack item2 = new ItemStack(Material.PAPER, 1);
		ItemMeta meta2 = item2.getItemMeta();
		List<String> lore2 = new ArrayList<String>();
		lore2.add(ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "Value: " + ChatColor.WHITE.toString());
		meta2.setLore(lore2);
		meta2.setDisplayName(ChatColor.GREEN.toString() + "Bank Note");
		item2.setItemMeta(meta2);
		net.minecraft.server.v1_8_R3.ItemStack nms2 = CraftItemStack.asNMSCopy(item2);
		NBTTagCompound tag2 = nms2.getTag() == null ? new NBTTagCompound() : nms2.getTag();
		tag2.setString("type", "money");
		tag2.setInt("worth", 0);
		nms2.setTag(tag2);
		banknote = CraftItemStack.asBukkitCopy(nms2);
	}

	public static ItemStack createBankNote(int ammount) {
		ItemStack item2 = new ItemStack(Material.PAPER, 1);
		ItemMeta meta2 = item2.getItemMeta();
		List<String> lore2 = new ArrayList<String>();
		lore2.add(ChatColor.WHITE.toString() + ChatColor.BOLD.toString() + "Value: " + ChatColor.WHITE.toString());
		meta2.setLore(lore2);
		meta2.setDisplayName(ChatColor.GREEN.toString() + "Bank Note");
		item2.setItemMeta(meta2);
		net.minecraft.server.v1_8_R3.ItemStack nms2 = CraftItemStack.asNMSCopy(item2);
		NBTTagCompound tag2 = nms2.getTag() == null ? new NBTTagCompound() : nms2.getTag();
		tag2.setString("type", "money");
		tag2.setInt("worth", ammount);
		nms2.setTag(tag2);
		return CraftItemStack.asBukkitCopy(nms2);
	}

	/**
	 * @param uuid
	 */
	public static void addGemsToPlayer(UUID uuid, int num) {
		DatabaseAPI.getInstance().update(uuid, EnumOperators.$INC, "info.gems", num);
	}

}
