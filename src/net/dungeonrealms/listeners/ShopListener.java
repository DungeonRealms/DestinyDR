package net.dungeonrealms.listeners;

import org.bukkit.ChatColor;
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
    public void playerRightClickChest(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.CHEST) return;
        Shop shop = ShopMechanics.getShop(block);
        if (shop == null) return;
        Action actionType = event.getAction();
        switch (actionType) {
            case RIGHT_CLICK_BLOCK:
                if (shop.isopen || shop.getOwner().getUniqueId() == event.getPlayer().getUniqueId()) {
                    event.setCancelled(true);
                    event.getPlayer().openInventory(shop.getInv());
                } else if (!shop.isopen) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED.toString() + "This shop is closed!");
                }
                break;
            case LEFT_CLICK_BLOCK:
                if (shop.getOwner().getUniqueId() == event.getPlayer().getUniqueId()) {
                    event.setCancelled(true);
                    shop.deleteShop();
                }
                break;
            default:
        }
    }

}
