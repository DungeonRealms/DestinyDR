package net.dungeonrealms.shops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.Lists;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.ItemSerialization;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;

/**
 * Created by Chase on Nov 17, 2015
 */
public class Shop {

	public UUID ownerUUID;
	public String ownerName;
	public Block block1;
	public Block block2;
	public Hologram hologram;
	public boolean isopen;
	public Inventory inventory;
	public String shopName;

	public Shop(UUID uuid, Location loc, String shopName) {
		this.ownerUUID = uuid;
		this.ownerName = getOwner().getName();
		this.block1 = loc.getWorld().getBlockAt(loc);
		this.block2 = loc.getWorld().getBlockAt(loc.add(1, 0, 0));
		this.shopName = shopName;
		hologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), loc.add(0, 1.5, .5));
		hologram.appendTextLine(ChatColor.RED.toString() + shopName);
		hologram.getVisibilityManager().setVisibleByDefault(true);
		isopen = false;
		inventory = createNewInv(ownerUUID);

	}

	private Inventory createNewInv(UUID uuid) {
		Inventory inv = Bukkit.createInventory(null, getInvSize(),
		        shopName + " - @" + Bukkit.getPlayer(uuid).getName());
		ItemStack button = new ItemStack(Material.INK_SACK, 1, DyeColor.GRAY.getDyeData());
		ItemMeta meta = button.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN.toString() + "Click to OPEN Shop");
		ArrayList<String> lore = new ArrayList<>();
		lore.add(ChatColor.GRAY + "This will open your shop to the public.");
		meta.setLore(lore);
		button.setItemMeta(meta);
		net.minecraft.server.v1_8_R3.ItemStack nmsButton = CraftItemStack.asNMSCopy(button);
		nmsButton.getTag().setString("status", "off");
		inv.setItem(8, CraftItemStack.asBukkitCopy(nmsButton));
		return inv;
	}

	public int getInvSize() {
		 int lvl = (int) DatabaseAPI.getInstance().getData(EnumData.SHOPLEVEL,
		 ownerUUID);
		return 9 * lvl;
	}

	public Player getOwner() {
		return Bukkit.getPlayer(ownerUUID);
	}

	/**
	 * Deletes block, and unregisters all things for shop.
	 *
	 * @since 1.0
	 */
	public void deleteShop() {
		DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.HASSHOP, false, true);
		hologram.delete();
		block1.setType(Material.AIR);
		block2.setType(Material.AIR);
		block1.getWorld().playSound(block1.getLocation(), Sound.PISTON_RETRACT, 1, 1);

		if(getOwner() == null){
			saveCollectionBin();
			block1.setType(Material.AIR);
			block2.setType(Material.AIR);
			block1.getWorld().playSound(block1.getLocation(), Sound.PISTON_RETRACT, 1, 1);
			block1.getWorld().save();
			ShopMechanics.ALLSHOPS.remove(ownerName);
		}else{
		for (int i = 0; i < inventory.getSize(); i++) {
			ItemStack current = inventory.getItem(i);
			if (current == null)
				continue;
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(current);
			if (nms.hasTag()) {
				if (nms.getTag().hasKey("status"))
					continue;
			}
			inventory.setItem(i, null);
			ItemMeta meta = current.getItemMeta();
			List<String> lore = meta.getLore();
			for (int j = 0; j < lore.size(); j++) {
				String currentStr = lore.get(j);
				if (currentStr.contains("Price")) {
					lore.remove(j);
					break;
				}
			}
			nms.getTag().remove("worth");
			meta.setLore(lore);
			current.setItemMeta(meta);
			if (getOwner() != null) {
				getOwner().getInventory().addItem(current);
			}
		}

		block1.getWorld().save();
		ShopMechanics.ALLSHOPS.remove(ownerName);
		}
	}

	/**
	* save to collection
	*/
	private void saveCollectionBin() {
		Inventory inv = Bukkit.createInventory(null, inventory.getSize(), "Collection Bin");
		int count = 0;
		for(ItemStack stack : inventory){
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
			if(stack != null && stack.getType() != Material.AIR){
				if(stack.getType() == Material.INK_SACK && nms.hasTag() && nms.getTag().hasKey("status"))
					continue;
    			ItemMeta meta = stack.getItemMeta();
    			List<String> lore = meta.getLore();
    			for (int j = 0; j < lore.size(); j++) {
    				String currentStr = lore.get(j);
    				if (currentStr.contains("Price")) {
    					lore.remove(j);
    					break;
    				}
    			}
    			if(nms.getTag().hasKey("worth"))
    			nms.getTag().remove("worth");
    			meta.setLore(lore);
    			stack.setItemMeta(meta);
    			
				inv.addItem(stack);
				count++;
			}
		}
		if(count > 0){
			String invToString = ItemSerialization.toString(inv);
			DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, invToString, true);
		}else{
			DatabaseAPI.getInstance().update(ownerUUID, EnumOperators.$SET, EnumData.INVENTORY_COLLECTION_BIN, "", true);
		}
	}

	/**
	 * @return
	 */
	public Inventory getInventory() {
		return inventory;
	}

	/**
	 * 
	 */
	public void updateStatus() {
		isopen = !isopen;
		hologram.clearLines();
		if (!isopen) {
			ItemStack button = new ItemStack(Material.INK_SACK, 1, DyeColor.GRAY.getDyeData());
			ItemMeta meta = button.getItemMeta();
			meta.setDisplayName(ChatColor.GREEN.toString() + "Click to OPEN Shop");
			ArrayList<String> lore = new ArrayList<>();
			lore.add(ChatColor.GRAY + "This will open your shop to the public.");
			meta.setLore(lore);
			button.setItemMeta(meta);
			net.minecraft.server.v1_8_R3.ItemStack nmsButton = CraftItemStack.asNMSCopy(button);
			nmsButton.getTag().setString("status", "off");
			inventory.setItem(8, CraftItemStack.asBukkitCopy(nmsButton));
			hologram.appendTextLine(ChatColor.RED.toString() +  shopName);
		} else {
			ItemStack button = new ItemStack(Material.INK_SACK, 1, DyeColor.LIME.getDyeData());
			ItemMeta meta = button.getItemMeta();
			meta.setDisplayName(ChatColor.RED.toString() + "Click to CLOSE Shop");
			ArrayList<String> lore = new ArrayList<>();
			lore.add(ChatColor.GRAY + "This will allow you to edit your stock.");
			meta.setLore(lore);
			button.setItemMeta(meta);
			net.minecraft.server.v1_8_R3.ItemStack nmsButton = CraftItemStack.asNMSCopy(button);
			nmsButton.getTag().setString("status", "on");
			inventory.setItem(8, CraftItemStack.asBukkitCopy(nmsButton));
			hologram.appendTextLine(ChatColor.GREEN.toString() + "[S] " + shopName);
		}
	}

}
