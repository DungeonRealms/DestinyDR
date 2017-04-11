package net.dungeonrealms.game.player.inventory.menus;

import org.bukkit.entity.Player;

import net.dungeonrealms.game.item.items.functional.ecash.ItemMountSkin;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;

public class ShopMenuMountSkin extends ShopMenu {

	public ShopMenuMountSkin(Player player) {
		super(player, "E-Cash Skins", 1);
	}

	@Override
	protected void setItems() {
		addItem(BACK);
		
		for (EnumMountSkins s : EnumMountSkins.values())
			addItem(new ItemMountSkin(s)).setECash(1250);
	}
}
