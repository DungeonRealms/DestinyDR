package net.dungeonrealms.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import net.dungeonrealms.shops.Shop;
import net.dungeonrealms.shops.ShopMechanics;
import net.md_5.bungee.api.ChatColor;

/**
 * Created by Chase on Sep 23, 2015
 */
public class ShopListener implements Listener {

	/**
	 * Handling Shops being Right clicked.
	 *
	 * @param PlayerInteractEvent
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

}
