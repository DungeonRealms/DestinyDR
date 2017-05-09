package net.dungeonrealms.game.player.inventory.menus;

import org.bukkit.entity.Player;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.item.items.functional.ItemFlightOrb;
import net.dungeonrealms.game.item.items.functional.ItemPeaceOrb;
import net.dungeonrealms.game.player.inventory.ShopMenu;

public class ShopItemVendor extends ShopMenu {

	public ShopItemVendor(Player player) {
		super(player, "Item Vendor", 2);
	}

	@Override
	protected void setItems() {
		PlayerWrapper pw = PlayerWrapper.getWrapper(getPlayer());
		if (pw.isInGuild())
            addItem(new VanillaItem(pw.getGuild().getBanner())).setUntradeable(true).setPrice(1000);
        
		addItem(new ItemFlightOrb()).setPrice(500);
		addItem(new ItemPeaceOrb()).setPrice(100);

		bloat();
	}
}
