package net.dungeonrealms.game.player.inventory.menus;

import org.bukkit.entity.Player;

import net.dungeonrealms.game.item.items.functional.ItemMuleUpgrade;
import net.dungeonrealms.game.item.items.functional.ItemProtectionScroll;
import net.dungeonrealms.game.mechanic.data.ShardTier;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.world.entity.type.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.item.Item.ItemTier;

public class ShopDungeoneer extends ShopMenu {

	public ShopDungeoneer(Player player) {
		super(player, "Dungeoneer", 1);
	}

	@Override
	protected void setItems() {
		
		for (int i = 1; i < MuleTier.values().length; i++) {
			MuleTier tier = MuleTier.values()[i];
			addItem(new ItemMuleUpgrade(tier)).setShards(tier.getPrice(), ShardTier.getByTier(tier.getTier() + 1));
		}
		
		for (ItemTier tier : ItemTier.values())
			addItem(new ItemProtectionScroll(tier)).setShards(1500, ShardTier.getByTier(tier.getId()));
	}
}
