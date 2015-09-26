package net.dungeonrealms.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.banks.Storage;
import net.dungeonrealms.duel.DuelMechanics;
import net.dungeonrealms.duel.DuelWager;
import net.dungeonrealms.items.Item;
import net.dungeonrealms.items.Item.ItemTier;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.shops.Shop;
import net.dungeonrealms.shops.ShopMechanics;
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

	/**
	 * Handling Shops inventory being clicked.
	 *
	 * @param event
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void shopInventoryClicked(InventoryClickEvent event) {
		if (event.getInventory().getTitle().contains("@")) {
			if (event.isShiftClick()) {
			event.setCancelled(true);
			return;
			}
			String owner = event.getInventory().getTitle().split("@")[1];
			Player shopOwner = Bukkit.getPlayer(owner);
			Player clicker = (Player) event.getWhoClicked();
			Shop shop = ShopMechanics.shops.get(shopOwner.getUniqueId());
			ItemStack item = event.getCurrentItem();
			if (item != null && item.getType() != Material.AIR) {
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
			if (nms != null) {
				if (clicker.getUniqueId() == shopOwner.getUniqueId()) {
					// Is OWner Clicking
					if (nms.hasTag() && nms.getTag().hasKey("status")) {
						// Clicking status off and on.
						event.setCancelled(true);
						if (nms.getTag().getString("status").equalsIgnoreCase("off")) {
                            shop.isopen = true;
                            shop.toggleHologram();
                            int slot = event.getRawSlot();
                            ItemStack button = new ItemStack(Material.INK_SACK, 1, DyeColor.LIME.getDyeData());
                            ItemMeta meta = button.getItemMeta();
                            meta.setDisplayName(ChatColor.RED.toString() + "Close Shop");
                            button.setItemMeta(meta);
                            net.minecraft.server.v1_8_R3.ItemStack nmsButton = CraftItemStack.asNMSCopy(button);
                            nmsButton.getTag().setString("status", "on");
                            shop.inventory.setItem(slot, CraftItemStack.asBukkitCopy(nmsButton));
                        } else {
						// Turn shop off;
                            shop.isopen = false;
                            shop.toggleHologram();
                            ItemStack button = new ItemStack(Material.INK_SACK, 1, DyeColor.GRAY.getDyeData());
                            ItemMeta meta = button.getItemMeta();
                            meta.setDisplayName(ChatColor.YELLOW.toString() + "Open Shop");
                            button.setItemMeta(meta);
                            net.minecraft.server.v1_8_R3.ItemStack nmsButton = CraftItemStack.asNMSCopy(button);
                            nmsButton.getTag().setString("status", "off");
                            shop.inventory.setItem(8, CraftItemStack.asBukkitCopy(nmsButton));
						}
					} else {
						// Clicking something not Turning shop off or
						// on.
						if (shop.isopen) {
						// make sure shop is off.
						    clicker.sendMessage(ChatColor.RED + "You must close the shop before you can edit");
						    event.setCancelled(true);
						} else {
						// shop is off
						    int slot = event.getRawSlot();
						    if (slot < shop.inventory.getSize()) {
                                ItemStack stackInSlot = event.getInventory().getItem(slot);
                                ItemStack itemHeld = event.getCursor();
                                if (stackInSlot != null && itemHeld.getType() != Material.AIR && itemHeld.getType() != stackInSlot.getType()) {
								// Swaping Items
								    clicker.sendMessage(ChatColor.RED.toString() + "Move item in slot first.");
								    event.setCancelled(true);
                                } else {
                                    if (event.isLeftClick()) {
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
                                        nms2.getTag().remove("worth");
                                        clicker.getInventory().addItem(CraftItemStack.asBukkitCopy(nms2));
                                        event.getInventory().setItem(slot, new ItemStack(Material.AIR, 1));
                                    } else if (event.isRightClick()) {
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
                                                    ItemMeta meta = stack.getItemMeta();
                                                    ArrayList<String> lore = new ArrayList<>();
                                                    lore.add(ChatColor.BOLD.toString() + ChatColor.GREEN.toString() + "Price: " + ChatColor.WHITE.toString() + number + "g");
                                                    meta.setLore(lore);
                                                    stack.setItemMeta(meta);
                                                    net.minecraft.server.v1_8_R3.ItemStack nms1 = CraftItemStack.asNMSCopy(stack);
                                                    nms1.getTag().setInt("worth", number);
                                                    shop.inventory.setItem(slot, CraftItemStack.asBukkitCopy(nms1));
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
                            }
                        }
                    }
                } else {
                    if(!shop.isopen){
                        clicker.closeInventory();
						clicker.sendMessage(ChatColor.RED.toString() + "This shop is closed!");
						return;
                    }
                    if (event.getRawSlot() < shop.inventory.getSize()) {
						event.setCancelled(true);
						if (event.isLeftClick()) {
						if (nms != null) {
							if (nms.getTag().hasKey("status"))
								return;
							int price = nms.getTag().getInt("worth");
							if (ShopMechanics.checkPlayerForMoney(clicker.getUniqueId(), price)) {
								ItemStack stack = item.clone();
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
								stack.setAmount(1);
								event.setCancelled(true);
								net.minecraft.server.v1_8_R3.ItemStack nms2 = CraftItemStack.asNMSCopy(stack);
								nms2.getTag().remove("worth");
								clicker.getInventory().addItem(CraftItemStack.asBukkitCopy(nms2));
								ItemStack tempStack = event.getInventory().getItem(event.getRawSlot());
								tempStack.setAmount(1);
								shop.inventory.remove(tempStack);
							} else {
								clicker.sendMessage("not enought money");
							}
                        }
                        }
					}
				}
            }
            } else { // Setting new item to shop
			if (event.getRawSlot() < shop.inventory.getSize()) {
				ItemStack itemHeld = event.getCursor();
				if (itemHeld.getType() == Material.AIR)
					return;
				Player player = clicker;
				if (player.getInventory().firstEmpty() < 0) {
					player.sendMessage("Make more room in your inventory");
					return;
				}
				event.setCancelled(true);
				event.setCursor(null);
				// player.getInventory().addItem(itemHeld);
				player.getInventory().setItem(player.getInventory().firstEmpty(), itemHeld);
				AnvilGUIInterface gui = AnvilApi.createNewGUI(player, event1 -> {
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
                            player.getPlayer().sendMessage("You can't ask for negative money!");
						} else {
                            ItemStack stack = itemHeld.clone();
                            ItemMeta meta = stack.getItemMeta();
                            ArrayList<String> lore = new ArrayList<>();
                            lore.add(ChatColor.BOLD.toString() + ChatColor.GREEN.toString() + "Price: " + ChatColor.WHITE.toString() + number);
                            meta.setLore(lore);
                            stack.setItemMeta(meta);
                            net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
                            nms.getTag().setInt("worth", number);
						if (shop.inventory.firstEmpty() >= 0) {
							shop.inventory.addItem(CraftItemStack.asBukkitCopy(nms));
							player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
							player.sendMessage(ChatColor.YELLOW.toString() + "Right click the item to edit price");
							player.sendMessage(ChatColor.RED.toString() + "Left click the item to remove");
							player.getInventory().remove(itemHeld);
						} else {
							player.getInventory().addItem(itemHeld);
							player.sendMessage("There is no room for this item in your Shop");
						}
                        }
                    }
				});
                ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("Price?");
				stack.setItemMeta(meta);
				gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
				player.closeInventory();
				gui.open();
			}
			}
		}
	}

	/**
	 * @param e
	 * @since 1.0 Handling wager inventory, when a player clicks the inventory.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDuelWagerClick(InventoryClickEvent e) {
		if (e.getInventory().getTitle().contains("vs.")) {
			if (e.isShiftClick()) {
                e.setCancelled(true);
                return;
			}
			Player p = (Player) e.getWhoClicked();
			DuelWager wager = DuelMechanics.getWager(p.getUniqueId());
			int slot = e.getRawSlot();
			ItemStack stack = e.getCurrentItem();
			if (stack == null)
			return;
			if (stack.getType() == Material.BONE) {
                e.setCancelled(true);
			} else if (slot == 30) {
                e.setCancelled(true);
                wager.cycleArmor();
			} else if (slot == 32) {
                e.setCancelled(true);
                wager.cycleWeapon();
			} else if (slot == 0) {
			if (wager.isLeft(p)) {
				// Left clicked
				e.setCancelled(true);
				if (CraftItemStack.asNMSCopy(stack).getTag().getString("state").equalsIgnoreCase("notready")) {
                    ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
						ChatColor.YELLOW.toString() + "Ready", null, DyeColor.LIME.getDyeData());
					net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("state", "ready");
					nms.setTag(nbt);
					nms.c(ChatColor.YELLOW.toString() + "Ready");
					wager.setItemSlot(0, CraftItemStack.asBukkitCopy(nms));
					if (CraftItemStack.asNMSCopy(e.getInventory().getItem(8)).getTag().getString("state").equalsIgnoreCase("ready")) {
						wager.startDuel();
					}
				} else {
					ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
						ChatColor.YELLOW.toString() + "Not Ready", null, DyeColor.GRAY.getDyeData());
					net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("state", "notready");
					nms.setTag(nbt);
					nms.c(ChatColor.YELLOW.toString() + "Not Ready");
					wager.setItemSlot(0, CraftItemStack.asBukkitCopy(nms));
				}
			} else {
				e.setCancelled(true);
			}
			} else if (slot == 8) {
			if (!wager.isLeft(p)) {
				// Right Clicked
				e.setCancelled(true);
				if (CraftItemStack.asNMSCopy(stack).getTag().getString("state").equalsIgnoreCase("notready")) {
					ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
						ChatColor.YELLOW.toString() + "Ready", null, DyeColor.LIME.getDyeData());
					net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("state", "ready");
					nms.setTag(nbt);
					nms.c(ChatColor.YELLOW.toString() + "Ready");
					wager.setItemSlot(8, CraftItemStack.asBukkitCopy(nms));
					if (CraftItemStack.asNMSCopy(e.getInventory().getItem(0)).getTag().getString("state")
						.equalsIgnoreCase("ready")) {
						wager.startDuel();
					}
				} else {
					ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
						ChatColor.YELLOW.toString() + "Not Ready", null, DyeColor.GRAY.getDyeData());
					net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("state", "notready");
					nms.setTag(nbt);
					nms.c(ChatColor.YELLOW.toString() + "Not Ready");
					wager.setItemSlot(8, CraftItemStack.asBukkitCopy(nms));
				}
			} else {
				e.setCancelled(true);
			}
			} else if (slot < 36) {
			if (e.isLeftClick()) {
				if (isLeftSlot(slot)) {
					if (wager.isLeft(p)) {

					} else {
						e.setCancelled(true);
					}
				} else {
					if (!wager.isLeft(p)) {
					} else {
						e.setCancelled(true);
					}
				}
			} else {
				e.setCancelled(true);
			}
			}
		}
	}

	/**
	 * @param slot
	 *           Check if slot is specified slot
	 */
	private boolean isLeftSlot(int slot) {
		int[] left = new int[] { 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21 };
        for (int aLeft : left)
            if (aLeft == slot)
                return true;
		return false;
	}

	/**
	 * @param event
	 * @since 1.0 Dragging is naughty.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDragItemInDuelWager(InventoryDragEvent event) {
		if (event.getInventory().getTitle().contains("vs.") || event.getInventory().getTitle().contains("Bank")
			|| event.getInventory().getTitle().contains("@"))
			event.setCancelled(true);
	}

	/**
	 * @param event
	 * @since 1.0 Called when a player swithced
	 */

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerSwitchItem(PlayerItemHeldEvent event) {
		if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;
		int slot = event.getNewSlot();
		if (event.getPlayer().getInventory().getItem(slot) != null) {
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(event.getPlayer().getInventory().getItem(slot));
			if (nms.hasTag()) {
                if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("weapon")) {
                    ItemTier tier = Item.ItemTier.getById(nms.getTag().getInt("itemTier"));
                    int minLevel = tier.getRangeValues()[0];
                    Player p = event.getPlayer();
                    int pLevel = (int) DatabaseAPI.getInstance().getData(EnumData.LEVEL, p.getUniqueId());
                    if (pLevel < minLevel) {
                        p.sendMessage(ChatColor.RED + "You must be level " + ChatColor.YELLOW.toString() + minLevel + ChatColor.RED.toString() + " to wield this weapon!");
                        event.setCancelled(true);
                    }
                }
            }
		}
	}

	/**
	 * @param event
	 * @since 1.0 Closes bother players wager inventory.
	 */

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDuelWagerClosed(InventoryCloseEvent event) {
		if (event.getInventory().getTitle().contains("vs.")) {
			Player p = (Player) event.getPlayer();
			DuelWager wager = DuelMechanics.getWager(p.getUniqueId());
			if (wager != null) {
                if (!wager.completed) {
                    wager.giveItemsBack();
                    DuelMechanics.removeWager(wager);
                    wager.p1.closeInventory();
                    wager.p2.closeInventory();
                }
			}
		}else if(event.getInventory().getTitle().contains("Storage Chest")){
			Storage  storage = BankMechanics.getStorage(event.getPlayer().getUniqueId());
			storage.inv.setContents(event.getInventory().getContents());
		}
	}
}
