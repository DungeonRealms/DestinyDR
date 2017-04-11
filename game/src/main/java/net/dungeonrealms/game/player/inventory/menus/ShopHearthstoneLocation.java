package net.dungeonrealms.game.player.inventory.menus;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.item.items.core.ShopItem;
import net.dungeonrealms.game.item.items.core.ShopItem.ShopItemClick;
import net.dungeonrealms.game.item.items.functional.ecash.ItemHearthStoneRelocator;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ShopHearthstoneLocation extends ShopMenu {
	
	public ShopHearthstoneLocation(Player player) {
		super(player, "Hearthstone Re-Location", 1);
	}

	@Override
	protected void setItems() {
		ShopItemClick cb = (player, item) -> {
			ItemHearthStoneRelocator h = (ItemHearthStoneRelocator)(item.getSoldItem());
			TeleportLocation currentHearthstone = TeleportLocation.valueOf(DatabaseAPI.getInstance().getData(EnumData.HEARTHSTONE, player.getUniqueId()).toString().toUpperCase());
			TeleportLocation newLocation = h.getLocation();
			if (currentHearthstone == newLocation) {
                player.sendMessage(ChatColor.RED + "Your Hearthstone is already at this location!");
                return false;
            }
			
			if (!newLocation.canSetHearthstone(player)) {
				player.sendMessage(ChatColor.RED + "You have not explored the surrounding area of this Hearthstone Location yet");
				return false;
			}
			
			DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.HEARTHSTONE, newLocation.name(), true);
			player.sendMessage(ChatColor.GREEN + "Hearthstone set to " + newLocation.getDisplayName() + ".");
			Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> player.closeInventory());
			return true;
		};
		
		
		for (TeleportLocation tl : TeleportLocation.values())
			if (tl.canBeABook())
				addItem(new ShopItem(new ItemHearthStoneRelocator(tl), cb)).setPrice(tl.getPrice());
	}
}
