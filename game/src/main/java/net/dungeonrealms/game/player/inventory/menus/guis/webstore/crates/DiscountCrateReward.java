package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.item.items.functional.ItemDiscountScroll;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 7/8/2017.
 */
public class DiscountCrateReward extends AbstractCrateReward {

    private int discountPercent;

    public DiscountCrateReward(int discountAmount) {
        super(Material.PAPER, ChatColor.GREEN.toString() + discountAmount + "% off the webstore", ChatColor.GRAY + "Right click to reveal the coupon code!");
        this.discountPercent = discountAmount;
    }

    @Override
    public void giveReward(Player player) {
        ItemStack coupon = new ItemDiscountScroll(discountPercent).generateItem();
        GameAPI.giveOrDropItem(player,coupon);
    }

    @Override
    public boolean canReceiveReward(Player player) {
        return true;
    }
}
