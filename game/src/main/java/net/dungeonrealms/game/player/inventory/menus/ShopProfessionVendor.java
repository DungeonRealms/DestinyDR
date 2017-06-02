package net.dungeonrealms.game.player.inventory.menus;

import net.dungeonrealms.game.item.items.core.ShopItem;
import org.bukkit.entity.Player;

import net.dungeonrealms.game.item.items.core.ItemFishingPole;
import net.dungeonrealms.game.item.items.core.ItemPickaxe;
import net.dungeonrealms.game.player.inventory.ShopMenu;

public class ShopProfessionVendor extends GUIMenu {

	public ShopProfessionVendor(Player player) {
		super(player, 9, "Profession Vendor");
		open(player, null);
	}

	@Override
	protected void setItems() {
		setItem(0, new ShopItem(new ItemPickaxe()).setGems(100));
		setItem(1, new ShopItem(new ItemFishingPole()).setGems(100));
	}
}
