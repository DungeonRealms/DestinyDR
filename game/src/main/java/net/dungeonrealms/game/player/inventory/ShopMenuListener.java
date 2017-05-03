package net.dungeonrealms.game.player.inventory;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
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
 * NOTE: This is NOT a player chest shop. However, it may be later.
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
		
		int playerEcash = (int) DatabaseAPI.getInstance().getData(EnumData.ECASH, p.getUniqueId());
		EnumData shards = shop.getShardTier() != null ? shop.getShardTier().getShardData() : null;
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
		if (shards != null && (int)DatabaseAPI.getInstance().getData(shards, p.getUniqueId()) < shop.getShards()) {
			p.sendMessage(ChatColor.RED + "You do not have " + shop.getShards() + " portal shards.");
			return;
		}
		
		boolean res = shop.getCallback().onClick(p, shop)
				&& (shop.getShards() > 0 || shop.getPrice() > 0 || shop.getECashCost() > 0);
		
		// Remove currency.
		if (res) {
			if (shop.getECashCost() > 0)
				DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$INC, EnumData.ECASH, -shop.getECashCost(), true);
			if (shop.getPrice() > 0)
				BankMechanics.takeGemsFromInventory(p, shop.getPrice());
			if (shards != null)
				DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$INC, shards, -shop.getShards(), true);
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
