package net.dungeonrealms.listeners;

import java.util.Arrays;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.duel.DuelMechanics;
import net.dungeonrealms.duel.DuelWager;
import net.dungeonrealms.mechanics.ItemManager;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * Created by Nick on 9/18/2015.
 */
public class InventoryListener implements Listener {

	/**
	 * Disables the clicking of items that contain NBTTag `important` in `type`.
	 * 
	 * @param event
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getCurrentItem() == null)
			return;
		net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getCurrentItem());
		if (nmsItem == null)
			return;
		NBTTagCompound tag = nmsItem.getTag();
		if (tag == null || !tag.getString("type").equalsIgnoreCase("important"))
			return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDuelWagerClick(InventoryClickEvent e) {
		if (e.getInventory().getTitle().contains("vs.")) {
			Player p = (Player) e.getWhoClicked();
			DuelWager wager = DuelMechanics.getWager(p);
			int slot = e.getRawSlot();
			ItemStack stack = e.getCurrentItem();
			if (stack.getType() == Material.BONE) {
			e.setCancelled(true);
			return;
			} else if (slot == 30) {
			wager.cycleArmor();
			} else if (slot == 32) {
			wager.cycleWeapon();
			} else if (slot == 0) {
			if (wager.isLeft(p)) {
				e.setCancelled(true);
				if (CraftItemStack.asNMSCopy(stack).getTag().getString("state").equalsIgnoreCase("notready")) {
					ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
						ChatColor.YELLOW.toString() + "Ready", null, DyeColor.LIME.getDyeData());
					net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("state", "ready");
					nms.setTag(nbt);
					wager.setItemSlot(0, CraftItemStack.asBukkitCopy(nms));
					// Check if other player ready
				} else {
					ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
						ChatColor.YELLOW.toString() + "Not Ready", null, DyeColor.GRAY.getDyeData());
					net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("state", "notready");
					nms.setTag(nbt);
					wager.setItemSlot(0, CraftItemStack.asBukkitCopy(nms));
				}
				// CHeck if other player ready
			} else {
				e.setCancelled(true);
				return;
			}
			} else if (slot == 8) {
			if (!wager.isLeft(p)) {
				e.setCancelled(true);
				if (CraftItemStack.asNMSCopy(stack).getTag().getString("state").equalsIgnoreCase("notready")) {
					ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
						ChatColor.YELLOW.toString() + "Ready", null, DyeColor.LIME.getDyeData());
					net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("state", "ready");
					nms.setTag(nbt);
					wager.setItemSlot(8, CraftItemStack.asBukkitCopy(nms));
					// Check if other player ready
				} else {
					ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
						ChatColor.YELLOW.toString() + "Not Ready", null, DyeColor.GRAY.getDyeData());
					net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("state", "notready");
					nms.setTag(nbt);
					wager.setItemSlot(8, CraftItemStack.asBukkitCopy(nms));
				}
			} else {
				e.setCancelled(true);
				return;
			}
			} else if (slot < 36) {
			if (isLeftSlot(slot) && wager.isLeft(p)) {

			} else {

			}
			}
		}
	}

	/**
	 * @param slot
	 * @return
	 */
	private boolean isLeftSlot(int slot) {
		int[] left = new int[] { 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21 };
		return Arrays.asList(left).contains(slot);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDuelWagerClosed(InventoryCloseEvent event) {
		if (event.getInventory().getTitle().contains("vs.")) {
			DuelWager wager = DuelMechanics.getWager((Player) event.getPlayer());
			wager.p1.closeInventory();
			wager.p2.closeInventory();
			DuelMechanics.removeWager(wager);
		}
	}
}
