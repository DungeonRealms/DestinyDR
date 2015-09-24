/**
 * 
 */
package net.dungeonrealms.shops;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.core.AnvilApiCore;
import com.minebone.anvilapi.nms.anvil.AnvilClickEvent;
import com.minebone.anvilapi.nms.anvil.AnvilClickEventHandler;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;

import net.md_5.bungee.api.ChatColor;

/**
 * Created by Chase on Sep 23, 2015
 */
public class ShopMechanics {
	public static HashMap<UUID, Shop> shops = new HashMap<UUID, Shop>();

	/**
	 * setup new shop for player
	 * @param block
	 * @param uniqueId
	 */
	public static void setupShop(Block block, UUID uniqueId) {
		Player player = Bukkit.getPlayer(uniqueId);
		AnvilGUIInterface gui = AnvilApi.createNewGUI(player, new AnvilClickEventHandler() {
			@Override
			public void onAnvilClick(final AnvilClickEvent event) {
			if (event.getSlot() == AnvilSlot.OUTPUT) {
				if (event.getName().equalsIgnoreCase("Shop Name?")) {
					event.setWillClose(true);
					event.setWillDestroy(true);
					player.sendMessage("Please enter a valid number");
					return;
				}
				String shopName;
				try {
					shopName = event.getName();
					if (shopName.length() > 12) {
						event.setWillClose(true);
						event.setWillDestroy(true);
						player.sendMessage(ChatColor.RED.toString() + "Shop name must be 12 characters.");
						return;
					}
				} catch (Exception exc) {
					event.setWillClose(true);
					event.setWillDestroy(true);
					Bukkit.getPlayer(event.getPlayerName()).sendMessage("Please enter a valid number");
					return;
				}
				Block b = player.getWorld().getBlockAt(block.getLocation().add(0, 1, 0));
				if (b.getType() == Material.AIR) {
					b.setType(Material.CHEST);
					Shop shop = new Shop(uniqueId, shopName, b);
					shops.put(uniqueId, shop);
					event.setWillClose(true);
					event.setWillDestroy(true);
				} else {
					player.sendMessage("You can't place a shop there");
					event.setWillClose(true);
					event.setWillDestroy(true);
				}
			}
			}
		});

		ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName("Shop Name?");
		stack.setItemMeta(meta);
		gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
		player.closeInventory();
		gui.open();

	}

	/**
	 * gets the shop represented by this block.
	 * @param block
	 */
	public static Shop getShop(Block block) {
		for (int i = 0; i < shops.values().size(); i++) {
			Shop current = (Shop) shops.values().toArray()[i];
			if (current.block.getX() == block.getX() && current.block.getY() == block.getY()
				&& current.getBlock().getZ() == block.getZ())
			return current;
		}
		return null;
	}

}
