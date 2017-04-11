package net.dungeonrealms.game.player.inventory.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.item.items.functional.ItemFlightOrb;
import net.dungeonrealms.game.item.items.functional.ItemPeaceOrb;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.inventory.ShopMenu;

public class ShopItemVendor extends ShopMenu {

	public ShopItemVendor(Player player) {
		super(player, "Item Vendor", 2);
	}

	@Override
	protected void setItems() {
		if (!GuildDatabaseAPI.get().isGuildNull(getPlayer().getUniqueId())) {
            String guildName = GuildDatabaseAPI.get().getGuildOf(getPlayer().getUniqueId());
            ItemStack banner = ItemManager.makeItemUntradeable(ItemSerialization.itemStackFromBase64(GuildDatabaseAPI.get().getBannerOf(guildName)));
            addItem(new VanillaItem(banner)).setPrice(1000);
        }
        
		addItem(new ItemFlightOrb()).setPrice(500);
		addItem(new ItemPeaceOrb()).setPrice(100);

		bloat();
	}
}
