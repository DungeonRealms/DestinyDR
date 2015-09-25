package net.dungeonrealms.mechanics;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 9/18/2015.
 */
public class PlayerManager {
    public static void checkInventory(Player player) {
        player.getInventory().setItem(7, ItemManager.createHearthStone("HearthStone", new String[]{
                ChatColor.GRAY + "(Right-Click) " + ChatColor.AQUA + "Back to your hearthstone location!"
        }));
        player.getInventory().setItem(8, ItemManager.getPlayerProfile(player, "Player Profile", new String[]{
                ChatColor.GRAY + "(Right-Click) " + ChatColor.AQUA + "Open your profile!"
        }));
    }
}
