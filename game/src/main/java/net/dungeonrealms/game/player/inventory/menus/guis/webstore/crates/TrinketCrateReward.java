package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.item.items.functional.accessories.Trinket;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketItem;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 7/10/2017.
 */
public class TrinketCrateReward extends AbstractCrateReward {

    Trinket reward;
    public TrinketCrateReward(Trinket reward) {
        super(reward.getType().getMaterial().getItemType(),reward.getType().getMaterial().getData(), reward.getPrefix() + " " + reward.getType().getName(), ChatColor.GRAY + "A trinket!");
        this.reward = reward;
    }

    @Override
    public void giveReward(Player player) {
        GameAPI.giveOrDropItem(player, new TrinketItem(reward.getType(), reward).generateItem());
    }

    @Override
    public boolean canReceiveReward(Player player) {
        return true;
    }
}
