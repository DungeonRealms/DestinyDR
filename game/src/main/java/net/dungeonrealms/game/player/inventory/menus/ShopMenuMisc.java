package net.dungeonrealms.game.player.inventory.menus;

import org.bukkit.entity.Player;

import net.dungeonrealms.game.item.items.functional.ItemBuff;
import net.dungeonrealms.game.item.items.functional.ecash.ItemGlobalMessager;
import net.dungeonrealms.game.item.items.functional.ecash.ItemRetrainingBook;
import net.dungeonrealms.game.mechanic.data.EnumBuff;
import net.dungeonrealms.game.player.inventory.ShopMenu;

public class ShopMenuMisc extends ShopMenu {
	
	public ShopMenuMisc(Player player) {
		super(player, "E-Cash Miscellaneous", 2);
	}

	@Override
	protected void setItems() {
//		addItem(BACK);
//		addItem(new ItemRetrainingBook()).setECash(550);
//		addItem(new ItemGlobalMessager()).setECash(200);
//
//		for (int i = 0; i < 3; i++)
//			for (EnumBuff buff : EnumBuff.values())
//				addItem(new ItemBuff(buff, 3600, 20 + (i * 15))).setECash(1000 + (i * 500));
	}
}
