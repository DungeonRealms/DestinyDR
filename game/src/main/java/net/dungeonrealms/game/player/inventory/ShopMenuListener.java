package net.dungeonrealms.game.player.inventory;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.core.ShopItem;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens for server-shop events.
 * 
 * Created April 10th, 2017.
 * @author Kneesnap
 */
public class ShopMenuListener implements Listener {

	@Getter
	private static Map<Player, ShopMenu> menus = new HashMap<>();
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent evt) {
		Player p = (Player) evt.getWhoClicked();
		
		if(!inShopGUI(p))
			return;
		
		evt.setCancelled(true);
		
		ItemStack clicked = evt.getCurrentItem();
		ShopMenu menu = getMenu(p);
		if (clicked == null || clicked.getType() == Material.AIR
				|| evt.getRawSlot() >= evt.getInventory().getSize() || !menu.getItems().containsKey(evt.getRawSlot()))
			return;
		
		ShopItem shop = menu.getItems().get(evt.getRawSlot());
		
		PlayerWrapper pw = PlayerWrapper.getPlayerWrapper(p);
		int playerEcash = pw.getEcash();
		
		//  ECASH  //
		if (playerEcash < shop.getECashCost()) {
			p.sendMessage(ChatColor.RED + "You need " + (shop.getECashCost() - playerEcash) + " more E-Cash.");
			return;
		}
		
		//  GEMS  //
		if (BankMechanics.getGemsInInventory(p) < shop.getPrice()) {
			p.sendMessage(ChatColor.RED + "You do not have " + shop.getPrice() + "g.");
			return;
		}
		
		//  SHARD  //
		if (shop.getShardTier() != null && pw.getPortalShards(shop.getShardTier()) < shop.getShards()) {
			p.sendMessage(ChatColor.RED + "You do not have " + shop.getShards() + " portal shards.");
			return;
		}
		
		boolean res = shop.getCallback().onClick(p, shop)
				&& (shop.getShards() > 0 || shop.getPrice() > 0 || shop.getECashCost() > 0);
		
		// Remove currency.
		if (res) {
			if (shop.getECashCost() > 0)
				pw.setEcash(pw.getEcash() - shop.getECashCost());
			if (shop.getPrice() > 0)
				BankMechanics.takeGemsFromInventory(p, shop.getPrice());
			if (shop.getShardTier() != null)
				GameAPI.removePortalShardsFromPlayer(p, shop.getShardTier(), shop.getShards());
			p.sendMessage(ChatColor.GREEN + "Transaction complete.");
			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent evt) {
		removeGui((Player) evt.getPlayer());
	}
	
	public void removeGui(Player player) {
		getMenus().remove(player);
	}
	
	/**
	 * Is the player in a shop gui?
	 */
	public static boolean inShopGUI(Player player) {
		return getMenus().containsKey(player);
	}
	
	/**
	 * Gets the GUI the player is looking in.
	 */
	public static ShopMenu getMenu(Player player) {
		return getMenus().get(player);
	}
}
