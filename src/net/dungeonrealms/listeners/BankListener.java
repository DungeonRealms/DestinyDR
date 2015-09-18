/**
 * 
 */
package net.dungeonrealms.listeners;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.md_5.bungee.api.ChatColor;

/**
 * Created by Chase on Sep 18, 2015
 */
public class BankListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEnderChestRightClick(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getClickedBlock().getType() == Material.ENDER_CHEST) {
				e.setCancelled(true);
				e.getPlayer().openInventory(getBank(e.getPlayer()));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBankClicked(InventoryClickEvent e) {	
		if (e.getInventory().getTitle().equalsIgnoreCase("Bank Chest")) {
			e.setCancelled(true);
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(e.getCurrentItem());
			if (e.getRawSlot() < 9) {
				// Bank Clicked
				if (e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null) {
					if (e.getCurrentItem().getItemMeta().getDisplayName().contains("Gem(s)")) {
						if (e.getClick() == ClickType.LEFT) {
							Player player = (Player) e.getWhoClicked();
							openHowManyGems(player);
						} else if (e.getClick() == ClickType.RIGHT) {

						}
					} else if (e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase("gem")) {
						if (nms.hasTag() && nms.getTag().hasKey("money")) {
							ItemStack stack = e.getInventory().getItem(e.getRawSlot());
							e.getInventory().remove(stack);
							Player player = (Player) e.getWhoClicked();
							BankMechanics.addGemsToPlayer(player, stack.getAmount());
							e.getInventory().getItem(8).getItemMeta().setDisplayName(
									DatabaseAPI.getInstance().getData(EnumData.GEMS, player.getUniqueId()) + " Gem(s)");
						}
					}
				}
			} else {
				// Player Inv Clicked
			}
		}
	}

	public boolean hasSpaceInInventory(Player p, int Gems_worth) {
		if (Gems_worth > 64) {
			int space_needed = Math.round(Gems_worth / 64) + 1;
			int count = 0;
			ItemStack[] contents = p.getInventory().getContents();
			for (ItemStack content : contents) {
				if (content == null || content.getType() == Material.AIR) {
					count++;
				}
			}
			int empty_slots = count;

			if (space_needed > empty_slots) {
				p.sendMessage(ChatColor.RED + "You do not have enough space in your inventory to withdraw " + Gems_worth
						+ " GEM(s).");
				p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "REQ: " + space_needed + " slots");
				return false;
			} else
				return true;
		}
		if (p.getInventory().firstEmpty() == -1)
			return false;
		return true;
	}

	public void openHowManyGems(Player player) {
		Inventory inv = Bukkit.createInventory(null, 9, "How Many?");
		
		inv.setItem(4, null);
		player.openInventory(inv);
	}

	
//	int number = 0;
//	int currentGems = getPlayerGems(player.getPlayer());
//	try {
//		number = Integer.parseInt(input);
//		if (number < 0) {
//			player.getPlayer().sendMessage("You can't ask for negative money!");
//		} else if (number > currentGems) {
//			player.getPlayer().sendMessage("You only have " + currentGems);
//		} else {
//			ItemStack stack = BankMechanics.gem.clone();
//			if (hasSpaceInInventory(player.getPlayer(), number)) {
//				Player p = player.getPlayer();
//				DatabaseAPI.getInstance().update(player.getPlayer().getUniqueId(), EnumOperators.$INC,
//						"gems", -number);
//				while (number > 0) {
//					while (number > 64) {
//						ItemStack item = stack.clone();
//						item.setAmount(64);
//						p.getInventory().setItem(p.getInventory().firstEmpty(), item);
//						number -= 64;
//					}
//					ItemStack item = stack.clone();
//					item.setAmount(number);
//					p.getInventory().setItem(p.getInventory().firstEmpty(), item);
//					number = 0;
//				}
//			} else {
//				player.getPlayer().sendMessage("You do not have space for all those gems");
//			}
//		}
//	} catch (Exception exc) {
//		player.getPlayer().sendMessage(input + " is not a number");
//		exc.printStackTrace();
//	}
	/**
	 * @param player
	 * @return
	 */
	private Inventory getBank(Player player) {
		Inventory inv = Bukkit.createInventory(null, 9, "Bank Chest");
		ItemStack bankItem = new ItemStack(Material.EMERALD);
		ItemMeta meta = bankItem.getItemMeta();
		meta.setDisplayName(getPlayerGems(player) + ChatColor.BOLD.toString() + ChatColor.GREEN + " Gem(s)");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GREEN.toString() + "Left Click " + " to withdraw Raw Gems.");
		lore.add(ChatColor.GREEN.toString() + "Right Click " + " to create a Bank Note.");
		// lore.add(ChatColor.GREEN.toString() + "Middle Click " + " to upgrade
		// your bank.");
		meta.setLore(lore);
		bankItem.setItemMeta(meta);
		inv.setItem(8, bankItem);
		return inv;
	}

	/**
	 * @param player
	 * @return
	 */
	private int getPlayerGems(Player player) {
		return (int) DatabaseAPI.getInstance().getData(EnumData.GEMS, player.getUniqueId());
	}

}
