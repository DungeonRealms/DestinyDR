package net.dungeonrealms.game.player.inventory.menus;

import org.bukkit.entity.Player;

import net.dungeonrealms.game.item.items.core.ItemFishingPole;
import net.dungeonrealms.game.item.items.core.ItemPickaxe;
import net.dungeonrealms.game.player.inventory.ShopMenu;

public class ShopProfessionVendor extends ShopMenu {

	public ShopProfessionVendor(Player player) {
		super(player, "Profession Vendor", 1);
	}

	@Override
	protected void setItems() {
		addItem(new ItemPickaxe()).setPrice(100);
		addItem(new ItemFishingPole()).setPrice(100);
	}
}
