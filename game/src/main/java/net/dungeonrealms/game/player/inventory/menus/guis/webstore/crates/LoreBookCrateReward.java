package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.item.items.functional.ecash.ItemLoreBook;
import net.dungeonrealms.game.item.items.functional.ecash.ItemRetrainingBook;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 7/10/2017.
 */
public class LoreBookCrateReward extends AbstractCrateReward {

    public LoreBookCrateReward() {
        super(Material.WRITTEN_BOOK, ChatColor.GREEN + "Lore Book", "");
    }

    @Override
    public void giveReward(Player player) {
        GameAPI.giveOrDropItem(player, new ItemLoreBook().generateItem());
    }

    @Override
    public boolean canReceiveReward(Player player) {
        return true;
    }
}
