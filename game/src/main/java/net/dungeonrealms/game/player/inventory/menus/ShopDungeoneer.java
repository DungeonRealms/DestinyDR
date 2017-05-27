package net.dungeonrealms.game.player.inventory.menus;

import net.dungeonrealms.game.item.items.core.ShopItem;
import net.dungeonrealms.game.item.items.functional.ItemMuleUpgrade;
import net.dungeonrealms.game.item.items.functional.ItemProtectionScroll;
import net.dungeonrealms.game.mechanic.data.MuleTier;
import net.dungeonrealms.game.mechanic.data.ShardTier;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import org.bukkit.entity.Player;

public class ShopDungeoneer extends GUIMenu {

    public ShopDungeoneer(Player player) {
        super(player, 9, "Dungeoneer");
        open(player, null);
    }

    @Override
    protected void setItems() {

        int index = 0;
        for (int i = 1; i < MuleTier.values().length; i++) {
            MuleTier tier = MuleTier.values()[i];
            setItem(index++, new ShopItem(new ItemMuleUpgrade(tier)).setShards(tier.getPrice(), ShardTier.getByTier(tier.getTier() + 1)));
        }

        for (ItemTier tier : ItemTier.values())
            setItem(index++, new ShopItem(new ItemProtectionScroll(tier)).setShards(1500, ShardTier.getByTier(tier.getId())));
    }
}
