package net.dungeonrealms.mechanics;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 9/18/2015.
 */
public class PlayerManager {

    /**
     * Ensures that every time the player logs in
     * the last 2 slots (7,8) have the correct items.
     *
     * @param player
     * @since 1.0
     */
    public static void checkInventory(Player player) {
        player.getInventory().setItem(7, ItemManager.createHearthStone("HearthStone", new String[]{
                ChatColor.GRAY + "(Right-Click) " + ChatColor.AQUA + "Back to your hearthstone location!"
        }));
        player.getInventory().setItem(8, ItemManager.getPlayerProfile(player, "Player Profile", new String[]{
                ChatColor.GRAY + "(Right-Click) " + ChatColor.AQUA + "Open your profile!"
        }));

    }
}
