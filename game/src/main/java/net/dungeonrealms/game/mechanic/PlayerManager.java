package net.dungeonrealms.game.mechanic;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.functional.ItemPlayerJournal;
import net.dungeonrealms.game.item.items.functional.ItemPortalRune;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * PlayerManager - Contains basic static utilities for players.
 * 
 * Redone in Late May 2017.
 * @author Kneesnap
 */
public class PlayerManager {

    /**
     * Ensures that every time the player logs in
     * the last slot (8) has the correct item.
     *
     * @param uuid
     * @since 1.0
     */
    public static void checkInventory(Player player) {
    	
        if (!hasItem(player.getInventory(), ItemType.PORTAL_RUNE) && isSlotFree(player.getInventory(), 7))
            player.getInventory().setItem(7, new ItemPortalRune(player).generateItem());

        if (!hasItem(player.getInventory(), ItemType.PLAYER_JOURNAL) && isSlotFree(player.getInventory(), 8))
        	player.getInventory().setItem(8, new ItemPlayerJournal().generateItem());
    }

    public static boolean isSlotFree(PlayerInventory inv, int slot) {
        ItemStack item = inv.getItem(slot);
        return (item == null || item.getType() == null || item.getType() == Material.AIR);
    }

    public static boolean hasItem(Player p, ItemType type) {
    	return hasItem(p.getInventory(), type);
    }
    
    public static boolean hasItem(PlayerInventory inv, ItemType type) {
        for (ItemStack item : inv.getContents())
            if (PersistentItem.isType(item, type))
            	return true;
        return false;
    }
}
