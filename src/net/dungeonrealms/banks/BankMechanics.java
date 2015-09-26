/**
 * 
 */
package net.dungeonrealms.banks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.dungeonrealms.DungeonRealms;
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

	public static void handleLogout(UUID uuid) {
		if (storage.containsKey(uuid)) {
			Inventory inv = storage.get(uuid).inv;
			try {
			String serializedInv = ItemSerialization.serialize(Arrays.asList(inv.getContents()));
			storage.remove(uuid);

			Player player = Bukkit.getPlayer(uuid);
			if (player.getItemInHand() != null) {
				List<ItemStack> items = Arrays.asList(inv.getContents());

				InputStream inputStream = null;
				OutputStream outputStream = null;

				try {
					File file = new File(
						DungeonRealms.getInstance().getDataFolder() + "/" + player.getName().toLowerCase() + ".json");
					if(file.exists())
						file.delete();
					file.createNewFile();
					String text = ItemSerialization.serialize(items);
					inputStream = IOUtils.toInputStream(text, "UTF-8");
					// read this file into InputStream
					outputStream = new FileOutputStream(file);

					int read = 0;
					byte[] bytes = new byte[1024];

					while ((read = inputStream.read(bytes)) != -1) {
						outputStream.write(bytes, 0, read);
					}

					System.out.println("Done!");

				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					player.setItemInHand(null);
					if (inputStream != null) {
						try {
						inputStream.close();
						} catch (IOException e) {
						e.printStackTrace();
						}
					}
					if (outputStream != null) {
						try {
						// outputStream.flush();
						outputStream.close();
						} catch (IOException e) {
						e.printStackTrace();
						}

					}
				}

			}

			// TODO Update MONGO with @serializedInv string.
			} catch (IOException e) {
			e.printStackTrace();
			}
		}
	}

	public static void handleLogin(UUID uuid) {

		// TODO SETUP MONGO
		// String serializedInv = "get from mongo";

		Player player = Bukkit.getPlayer(uuid);
		File file = new File(
			DungeonRealms.getInstance().getDataFolder() + "/" + player.getName().toLowerCase() + ".json");
		if (file.exists()){
			try {
			InputStream inputStream = new FileInputStream(file);
			String source = IOUtils.toString(inputStream, "UTF-8");
			List<ItemStack> items = ItemSerialization.deserialize(source);
//			items.stream().filter(item -> item != null).forEach(item -> player.getInventory().addItem(item));
			
			Storage storageTemp = new Storage(uuid, items);
			storage.put(uuid, storageTemp);
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}else{
			Storage storageTemp = new Storage(uuid);
			storage.put(uuid, storageTemp);
		}
		// List<ItemStack> contents =
		// ItemSerialization.deserialize(serializedInv);
	}

	/**
	 * @return
	 */
	private static void loadCurrency() {
		ItemStack item = new ItemStack(Material.EMERALD, 1);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new ArrayList<>();
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
		List<String> lore2 = new ArrayList<>();
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
		List<String> lore2 = new ArrayList<>();
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

	/**
	 * @param uniqueId
	 */
	public static Storage getStorage(UUID uniqueId) {
		return storage.get(uniqueId);
	}

}
