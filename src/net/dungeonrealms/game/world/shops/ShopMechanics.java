package net.dungeonrealms.game.world.shops;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;

import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.inventory.NPCMenus;

/**
 * Created by Chase on Nov 17, 2015
 */
public class ShopMechanics implements GenericMechanic{

	public static ConcurrentHashMap<String, Shop> ALLSHOPS = new ConcurrentHashMap<>();

	public static Shop getShop(Block block) {
		for (Shop shop : ALLSHOPS.values()) {
			if (shop.block1.getX() == block.getX() && shop.block1.getY() == block.getY()
			        && shop.block1.getZ() == block.getZ()
			        || shop.block2.getX() == block.getX() && shop.block2.getY() == block.getY()
			                && shop.block2.getZ() == block.getZ()) {
				return shop;
			}
		}
		return null;
	}

	public static void deleteAllShops() {
		ALLSHOPS.values().forEach(net.dungeonrealms.game.world.shops.Shop::deleteShop);
		Bukkit.getWorlds().get(0).save();
	}

	public static void setupShop(Block block, UUID uniqueId) {
		Player player = Bukkit.getPlayer(uniqueId);
		AnvilGUIInterface gui = AnvilApi.createNewGUI(player, event -> {
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
					if (shopName.length() > 14) {
						event.setWillClose(true);
						event.setWillDestroy(true);
						player.sendMessage(ChatColor.RED.toString() + "Shop name must be less than 14 characters.");
						return;
					} else if (shopName.length() <= 2) {
						event.setWillClose(true);
						event.setWillDestroy(true);
						player.sendMessage(ChatColor.RED.toString() + "Shop name must be at least 3 characters.");
						return;
					}
				} catch (Exception exc) {
					event.setWillClose(true);
					event.setWillDestroy(true);
					Bukkit.getPlayer(event.getPlayerName()).sendMessage("Please enter a valid number");
					return;
				}
				Block b = player.getWorld().getBlockAt(block.getLocation().add(0, 1, 0));
				Block block2 = block.getWorld().getBlockAt(block.getLocation().add(1, 1, 0));
				if (b.getType() == Material.AIR && block2.getType() == Material.AIR) {
					block2.setType(Material.CHEST);
					b.setType(Material.CHEST);
					Shop shop = new Shop(uniqueId, b.getLocation(), shopName);
					DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$SET, EnumData.HASSHOP, true, true);
					ALLSHOPS.put(player.getName(), shop);
					player.sendMessage(ChatColor.YELLOW + ChatColor.BOLD.toString() + "YOU'VE CREATED A SHOP!");
					player.sendMessage(ChatColor.YELLOW + "To stock your shop, simply drag items into your shop's inventory.");
					event.setWillClose(true);
					event.setWillDestroy(true);
				} else {
					player.sendMessage("You can't place a shop there");
					event.setWillClose(true);
					event.setWillDestroy(true);
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
	 * @param ownerName
	 * @return
	 */
	public static Shop getShop(String ownerName) {
		return ALLSHOPS.get(ownerName);
	}
	
	@Override
	public EnumPriority startPriority() {
		return EnumPriority.NO_STARTUP;
	}

	@Override
	public void startInitialization() {

	}

	@Override
	public void stopInvocation() {
      deleteAllShops();
	}
	
	/**
	 * @param item 
	 * @param price
	 * @return
	 */
	public static ItemStack addPrice(ItemStack item, int price) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + price + "g");
        String[] arr = lore.toArray(new String[lore.size()]);
        item = NPCMenus.editItem(item, item.getItemMeta().getDisplayName(), arr);
        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
        nms.getTag().setInt("worth", price);
		return CraftItemStack.asBukkitCopy(nms);
	}
}
