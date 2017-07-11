package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.item.items.functional.ItemTeleportBook;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 7/9/2017.
 */
public class TeleportBookCrateReward extends AbstractCrateReward {

    public TeleportBookCrateReward() {
        super(Material.BOOK, ChatColor.GREEN + "Teleport Book", ChatColor.GRAY + "A teleport book to a random location");
    }

    @Override
    public void giveReward(Player player) {
        GameAPI.giveOrDropItem(player, new ItemTeleportBook().generateItem());
    }

    @Override
    public boolean canReceiveReward(Player player) {
        return true;
    }
}
