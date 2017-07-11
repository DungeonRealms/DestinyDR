package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 7/7/2017.
 */
public class ItemStackCrateReward extends AbstractCrateReward {

    private ItemStack rewardStack;
    public ItemStackCrateReward(ItemStack reward) {
        super(reward.getType(), Utils.getItemName(reward), Utils.getLore(reward));
        this.rewardStack = reward;
    }

    @Override
    public void giveReward(Player player) {
        GameAPI.giveOrDropItem(player,rewardStack);
    }

    @Override
    public boolean canReceiveReward(Player player) {
        return true;
    }
}
