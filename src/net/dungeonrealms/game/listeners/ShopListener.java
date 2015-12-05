package net.dungeonrealms.game.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;

import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.network.NetworkAPI;
import net.dungeonrealms.game.world.shops.Shop;
import net.dungeonrealms.game.world.shops.ShopMechanics;

/**
 * Created by Chase on Sep 23, 2015
 */
public class ShopListener implements Listener {

	/**
	 * Handling Shops being Right clicked.
	 *
	 * @param event
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerRightClickChest(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (block == null)
			return;
		if (block.getType() != Material.CHEST)
			return;
		Shop shop = ShopMechanics.getShop(block);
		if (shop == null)
			return;
		Action actionType = event.getAction();
		switch (actionType) {
		case RIGHT_CLICK_BLOCK:
			if (shop.isopen || shop.ownerUUID.toString().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
				event.setCancelled(true);
				event.getPlayer().openInventory(shop.getInventory());
			} else if (!shop.isopen) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED.toString() + "This shop is closed!");
			}
			break;
		case LEFT_CLICK_BLOCK:
			if (shop.ownerUUID.toString().equalsIgnoreCase(event.getPlayer().getUniqueId().toString())) {
				event.setCancelled(true);
				shop.deleteShop();
			}
			break;
		default:
		}
	}
	//RANDOM MESSAGE NIGGAA BOY!
	
	/**
	 * Ex - Fin -ehty is a nigger boy.
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerClickShopInventory(InventoryClickEvent event) {
		if (!event.getInventory().getTitle().contains("@"))
			return;
		if (event.isShiftClick()) {
			event.setCancelled(true);
			return;
		}
		String ownerName = event.getInventory().getTitle().split("@")[1];
		if (ownerName == null)
			return;
		Shop shop = ShopMechanics.getShop(ownerName);
		if (shop == null)
			return;
		if (event.getAction() == InventoryAction.NOTHING)
			return;
		Player clicker = (Player) event.getWhoClicked();
		if (event.getRawSlot() >= event.getInventory().getSize()) {
			return;
		}
		if (event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) {
			event.setCancelled(true);
			return;
		}
		if (clicker.getUniqueId().toString().equalsIgnoreCase(shop.ownerUUID.toString())) {
			// Owner is Clicking
			if (event.getRawSlot() == 8) {
				event.setCancelled(true);
				((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.LEVEL_UP, 1, 1)	;
				shop.updateStatus();
				return;
			}
			ItemStack itemHeld = event.getCursor();
			ItemStack stackInSlot = event.getCurrentItem();
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(itemHeld);
			if (shop.isopen) {
				clicker.sendMessage(ChatColor.RED + "You must close the shop before you can edit");
				event.setCancelled(true);
				return;
			}
			if (stackInSlot != null && stackInSlot.getType() != Material.AIR && itemHeld.getType() != Material.AIR
			        && itemHeld.getType() != stackInSlot.getType()) {
				clicker.sendMessage(ChatColor.RED.toString() + "Move item in slot first.");
				event.setCancelled(true);
				return;
			} else {
				if (event.isLeftClick()) {
					if (stackInSlot == null || stackInSlot.getType() == Material.AIR) {
						//Setting new Item in SHop
						if (itemHeld.getType() == Material.AIR || itemHeld == null || itemHeld.getType() == Material.EMERALD)
							return;
						if (clicker.getInventory().firstEmpty() < 0) {
							clicker.sendMessage("Make more room in your inventory");
							event.setCancelled(true);
							event.setCursor(null);
							return;
						}
						if (nms.hasTag() && nms.getTag().hasKey("subtype")
						        && nms.getTag().getString("subtype").equalsIgnoreCase("starter")) {
							event.setCancelled(true);
							clicker.sendMessage("Can't sell starter Items!");
							return;
						}
						event.setCancelled(true);
						event.setCursor(null);
						int playerSlot = clicker.getInventory().firstEmpty();
						clicker.getInventory().setItem(playerSlot, itemHeld);
						AnvilGUIInterface gui = AnvilApi.createNewGUI(clicker, event1 -> {
							if (event1.getSlot() == AnvilSlot.OUTPUT) {
								int number = 0;
								try {
									number = Integer.parseInt(event1.getName());
								} catch (Exception exc) {
									event1.setWillClose(true);
									event1.setWillDestroy(true);
									Bukkit.getPlayer(event1.getPlayerName()).sendMessage("Please enter a valid number");
									return;
								}
								event1.setWillClose(true);
								event1.setWillDestroy(true);
								if (number < 0) {
									clicker.sendMessage("You can't ask for negative money!");
								} else {
									ItemStack stack = itemHeld.clone();
									ItemMeta meta = itemHeld.getItemMeta();
									ArrayList<String> lore = new ArrayList<>();
									if (meta.hasLore()) {
										lore = (ArrayList<String>) meta.getLore();
									}
									lore.add(ChatColor.BOLD.toString() + ChatColor.GREEN.toString() + "Price: "
						                    + ChatColor.WHITE.toString() + number + "g");
									meta.setLore(lore);
									stack.setItemMeta(meta);
									net.minecraft.server.v1_8_R3.ItemStack newNMS = CraftItemStack.asNMSCopy(stack);
									newNMS.getTag().setInt("Price", number);
									if (shop.inventory.firstEmpty() >= 0) {
										int slot = shop.inventory.firstEmpty();
										shop.inventory.setItem(slot, CraftItemStack.asBukkitCopy(newNMS));
										clicker.playSound(clicker.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);

										clicker.sendMessage(new String[] {
						                        ChatColor.YELLOW.toString() + "Price set. Right-Click item to edit.",
						                        ChatColor.YELLOW + "Left Click the item to remove it from your shop." });
										clicker.getInventory().setItem(playerSlot, new ItemStack(Material.AIR));
									} else {
										clicker.getInventory().addItem(itemHeld);
										clicker.sendMessage("There is no room for this item in your Shop");
									}
								}
							}
						});
						ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
						ItemMeta meta = stack.getItemMeta();
						meta.setDisplayName("Price?");
						stack.setItemMeta(meta);
						gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
						clicker.closeInventory();
						gui.open();
						return;
					}
					// Removing item from Shop
					ItemStack stack = stackInSlot.clone();
					ItemMeta meta = stack.getItemMeta();
					List<String> lore = meta.getLore();
					if (lore != null)
						for (int i = 0; i < lore.size(); i++) {
							String current = lore.get(i);
							if (current.contains("Price")) {
								lore.remove(i);
								break;
							}
						}
					meta.setLore(lore);
					stack.setItemMeta(meta);
					event.setCancelled(true);
					net.minecraft.server.v1_8_R3.ItemStack nms2 = CraftItemStack.asNMSCopy(stack);
					nms2.getTag().remove("Price");
					clicker.getInventory().addItem(CraftItemStack.asBukkitCopy(nms2));
					event.getInventory().setItem(event.getRawSlot(), new ItemStack(Material.AIR, 1));
				} else if (event.isRightClick()) {
					if(stackInSlot == null || stackInSlot.getType() == Material.AIR){
						clicker.sendMessage(ChatColor.RED + "Can't edit an empty shop!");
						event.setCancelled(true);
						return;
					}
					event.setCancelled(true);
					Player player = clicker;
					player.closeInventory();
					AnvilGUIInterface gui = AnvilApi.createNewGUI(player, e -> {
						if (e.getSlot() == AnvilSlot.OUTPUT) {
							int number = 0;
							try {
								number = Integer.parseInt(e.getName());
								player.sendMessage("Price set");
							} catch (Exception exc) {
								e.setWillClose(true);
								e.setWillDestroy(true);
								Bukkit.getPlayer(e.getPlayerName()).sendMessage("Please enter a valid number");
								return;
							}
							if (number < 0) {
								player.getPlayer().sendMessage("You can't ask for negative money!");
							} else {
								ItemStack stack = stackInSlot.clone();
								ItemMeta meta = stackInSlot.getItemMeta();
								ArrayList<String> lore = new ArrayList<>();
								if (meta.hasLore()) {
									lore = (ArrayList<String>) meta.getLore();
								}
								for (int i = 0; i < lore.size(); i++) {
									String current = lore.get(i);
									if (current.contains("Price")) {
										lore.remove(i);
										break;
									}
								}
								lore.add(ChatColor.BOLD.toString() + ChatColor.GREEN.toString() + "Price: "
					                    + ChatColor.WHITE.toString() + number + "g");
								meta.setLore(lore);
								stack.setItemMeta(meta);
								net.minecraft.server.v1_8_R3.ItemStack nms1 = CraftItemStack.asNMSCopy(stack);
								nms1.getTag().setInt("Price", number);
								shop.inventory.setItem(event.getRawSlot(), CraftItemStack.asBukkitCopy(nms1));
								player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
								e.setWillClose(true);
								e.setWillDestroy(true);
							}
						}
					});
					ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
					ItemMeta meta = stack.getItemMeta();
					meta.setDisplayName("Price?");
					stack.setItemMeta(meta);
					gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
					gui.open();
				}
			}
		} else {
			event.setCancelled(true);
			// Not Owner Clicking
			if (!shop.isopen) {
				if(event.getCursor() != null){
					clicker.getInventory().addItem(event.getCursor());
					event.setCursor(null);
				}
				clicker.closeInventory();
				clicker.sendMessage(ChatColor.RED + "The shop has closed");
				return;
			}
			if (event.getRawSlot() == 8) {
				return;
			}
			ItemStack itemClicked = event.getCurrentItem();
			if (itemClicked == null || itemClicked.getType() == Material.AIR)
				return;
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(itemClicked);
			if (nms == null || !nms.hasTag() || !nms.getTag().hasKey("Price"))
				return;
			int itemPrice = nms.getTag().getInt("Price");

			if (BankMechanics.getInstance().takeGemsFromInventory(itemPrice, clicker)) {
				if (itemClicked.getAmount() == 1) {
					event.getInventory().setItem(event.getRawSlot(), new ItemStack(Material.AIR));
				} else {
					event.getInventory().getItem(event.getRawSlot()).setAmount(itemClicked.getAmount() - 1);
				}
				ItemStack clickClone = itemClicked.clone();
				ItemMeta meta = clickClone.getItemMeta();
				List<String> lore = meta.getLore();
				if (lore != null)
					for (int i = 0; i < lore.size(); i++) {
						String current = lore.get(i);
						if (current.contains("Price")) {
							lore.remove(i);
							break;
						}
					}
				meta.setLore(lore);
				clickClone.setItemMeta(meta);
				DatabaseAPI.getInstance().update(shop.ownerUUID, EnumOperators.$INC, EnumData.GEMS, itemPrice, true);
				if(shop.getOwner() != null){
					shop.getOwner().sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "  +" + itemPrice + " gems from shop");
				}else{
				NetworkAPI.getInstance().sendPlayerMessage(ownerName,
				        ChatColor.GREEN.toString() + ChatColor.BOLD + "  +" + itemPrice + " gems from shop");
				}
				clickClone.setAmount(1);
				clicker.getInventory().addItem(clickClone);
				clicker.sendMessage(ChatColor.GREEN + "You purchased a " + ChatColor.YELLOW
				        + itemClicked.getItemMeta().getDisplayName() + ChatColor.GREEN + " for " + ChatColor.YELLOW
				        + itemPrice + ChatColor.GREEN + " gems");
			} else {
				clicker.closeInventory();
				clicker.sendMessage(ChatColor.RED + "You do not have " + itemPrice + " gems");
			}
		}
	}

	public void punchShop(PlayerInteractEvent event) {
		if (event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;
		if (event.getClickedBlock().getType() != Material.CHEST)
			return;
		Shop shop = ShopMechanics.getShop(event.getClickedBlock());
		if (shop == null)
			return;
		if (event.getPlayer().getUniqueId().toString().equalsIgnoreCase(shop.ownerUUID.toString()))
			shop.deleteShop();
		if(event.getPlayer().isOp()){
			shop.deleteShop();
		}
	}

}
