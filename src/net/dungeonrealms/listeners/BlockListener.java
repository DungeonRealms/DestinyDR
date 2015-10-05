package net.dungeonrealms.listeners;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.shops.Shop;
import net.dungeonrealms.shops.ShopMechanics;
import net.dungeonrealms.spawning.MobSpawner;
import net.dungeonrealms.spawning.SpawningMechanics;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

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
	 * Handles breaking a shop
	 *
	 * @param e
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void blockBreak(BlockBreakEvent e) {
		Block block = e.getBlock();
		if (block == null)
			return;
		if (block.getType() == Material.CHEST) {
			Shop shop = ShopMechanics.getShop(block);
			if (shop == null) {
			return;
			} else {
			e.setCancelled(true);
			if (e.getPlayer().isOp()) {
				shop.deleteShop();
			}
			}
		} else if (block.getType() == Material.ARMOR_STAND) {
			ArrayList<MobSpawner> list = SpawningMechanics.getSpawners();
			for (int i = 0; i < list.size(); i++) {
			MobSpawner current = list.get(i);
			if (current.loc == block.getLocation()) {
				SpawningMechanics.remove(i);
			}
			}
		} else {
			return;
		}

	}

	/**
	 * Handling Shops being Right clicked.
	 *
	 * @param e
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerRightClickChest(PlayerInteractEvent e) {
		Block block = e.getClickedBlock();
		if (block == null)
			return;
		if (block.getType() != Material.CHEST)
			return;
		Shop shop = ShopMechanics.getShop(block);
		if (shop == null)
			return;
		Action actionType = e.getAction();
		switch (actionType) {
		case RIGHT_CLICK_BLOCK:
			if (shop.isopen || shop.getOwner().getUniqueId() == e.getPlayer().getUniqueId()) {
			e.setCancelled(true);
			e.getPlayer().openInventory(shop.getInv());
			} else if (!shop.isopen) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED.toString() + "This shop is closed!");
			}
			break;
		case LEFT_CLICK_BLOCK:
			if (shop.getOwner().getUniqueId() == e.getPlayer().getUniqueId()) {
			e.setCancelled(true);
			shop.deleteShop();
			}
			break;
		default:
		}
	}

	/**
	 * Handling setting up shops.
	 *
	 * @param event
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockDamaged(PlayerInteractEvent event) {
		if (event.getItem() == null)
			return;
		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			if (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR)
			return;
			net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getItem());
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
					event.getPlayer()
						.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "You already have an active shop");
					return;
				}
				ShopMechanics.setupShop(event.getClickedBlock(), event.getPlayer().getUniqueId());
			}
			}
		}
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void snowmanMakeSnow(EntityBlockFormEvent event) {
		if (event.getNewState().getType() == Material.SNOW) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> event.getBlock().setType(Material.AIR), 60L);
		} else {
			event.setCancelled(true);
		}
	}
}
