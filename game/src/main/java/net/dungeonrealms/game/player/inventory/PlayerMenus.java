package net.dungeonrealms.game.player.inventory;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

/**
 * Created by Nick on 9/29/2015.
 */
public class PlayerMenus {
    public static ItemStack editItem(String playerName, String name, String[] lore) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwner(playerName);
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack editItem(ItemStack itemStack, String name, String[] lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }

    public static ItemStack editItemWithShort(ItemStack itemStack, short shortID, String name, String[] lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        itemStack.setDurability(shortID);
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }

    public static ItemStack editItem(ItemStack itemStack, String[] lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }
}