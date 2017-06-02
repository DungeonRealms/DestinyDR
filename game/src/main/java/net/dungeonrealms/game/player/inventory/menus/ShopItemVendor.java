package net.dungeonrealms.game.player.inventory.menus;

import net.dungeonrealms.game.item.items.core.ShopItem;
import org.bukkit.entity.Player;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.item.items.functional.ItemFlightOrb;
import net.dungeonrealms.game.item.items.functional.ItemPeaceOrb;
import net.dungeonrealms.game.player.inventory.ShopMenu;

public class ShopItemVendor extends GUIMenu {

	public ShopItemVendor(Player player) {
		super(player, 9, "Item Vendor");
		open(player, null);
	}

	@Override
	protected void setItems() {
		PlayerWrapper pw = PlayerWrapper.getWrapper(getPlayer());
		if (pw.isInGuild())
            addItem(new VanillaItem(pw.getGuild().getBanner())).setUntradeable(true).setPrice(1000);

		setItem(0, new ShopItem(new ItemFlightOrb()).setGems(100));
		setItem(1, new ShopItem(new ItemPeaceOrb()).setGems(100));
	}
}
