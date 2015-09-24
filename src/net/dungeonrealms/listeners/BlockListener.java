package net.dungeonrealms.listeners;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.shops.ShopMechanics;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * Created by Nick on 9/18/2015.
 */
public class BlockListener implements Listener {

	/**
	 * Disables the placement of core items that have NBTData of `important` in
	 * `type` field.
	 * 
	 * @param event
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getItemInHand() == null)
			return;
		net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getItemInHand());
		if (nmsItem == null)
			return;
		NBTTagCompound tag = nmsItem.getTag();
		if (tag == null || !tag.getString("type").equalsIgnoreCase("important"))
			return;
		event.setCancelled(true);
	}

	/**
	 * Handling shop breaks, and setting up shops.
	 * 
	 * @param event
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockDamaged(BlockDamageEvent event) {
		if (event.getItemInHand() == null)
			return;
		net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getItemInHand());
		if (nmsItem == null)
			return;
		NBTTagCompound tag = nmsItem.getTag();
		if (tag == null || !tag.getString("type").equalsIgnoreCase("important"))
			return;
		event.setCancelled(true);
		if (event.getPlayer().isSneaking()) {
			ItemStack item = event.getPlayer().getItemInHand();
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
			if (nms.getTag().hasKey("usage") && nms.getTag().getString("usage").equalsIgnoreCase("profile")) {
			if (ShopMechanics.shops.get(event.getPlayer().getUniqueId()) != null) {
				event.getPlayer().sendMessage(ChatColor.RED + " You already have an active shop");
				return;
			}
			ShopMechanics.setupShop(event.getBlock(), event.getPlayer().getUniqueId());
			}
		}
	}
}
