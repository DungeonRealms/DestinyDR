package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import net.dungeonrealms.common.Constants;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 5/10/2017.
 */
public interface WebstoreGUI {

    WebstoreCategories getCategory();

    default void sendNotUnlocked(Player player) {
        player.sendMessage(ChatColor.RED + "You do not own this item!");
        player.sendMessage(ChatColor.GRAY + "You can unlock it at " + ChatColor.UNDERLINE + Constants.SHOP_URL);
    }

}
