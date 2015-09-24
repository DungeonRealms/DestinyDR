/**
 * 
 */
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
	public void playerRightClickChest(PlayerInteractEvent e) {
		Block block = e.getClickedBlock();
		if (block != null) {
			if (block.getType() == Material.CHEST) {
			Shop shop = ShopMechanics.getShop(block);
			if (shop != null) {
				if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
					if (shop.isopen || shop.getOwner().getUniqueId() == e.getPlayer().getUniqueId()) {
						e.setCancelled(true);
						e.getPlayer().openInventory(shop.getInv());
					}
				} else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
					if (shop.getOwner().getUniqueId() == e.getPlayer().getUniqueId()) {
						e.setCancelled(true);
						shop.deleteShop();
					}
				}
			}
			}
		}
	}

}
