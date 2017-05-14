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

    /**
     * Opens the GM Toggles menu.
     * (user must be GM)
     *
     * @param player
     */
    public static void openGameMasterTogglesMenu(Player player) {
        if (!Rank.isTrialGM(player)) return;
        boolean isToggled = false;

        Inventory inv = Bukkit.createInventory(null, 9, "Game Master Toggles");
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (gp == null) return;

        // Invisible
        isToggled = GameAPI._hiddenPlayers.contains(player);
        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, (short) (isToggled ? 10 : 8)), (isToggled ? ChatColor.GREEN : ChatColor.RED) + "Invisible Mode", new String[]{
                ChatColor.GRAY + "Toggling this will make you invisible to players and mobs.",
                ChatColor.GRAY + "Display Item"}).build());

        // Allow Fight
        isToggled = !gp.isInvulnerable() && gp.isTargettable();
        inv.setItem(1, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, (short) (isToggled ? 10 : 8)), (isToggled ? ChatColor.GREEN : ChatColor.RED) + "Allow Combat", new String[]{
                ChatColor.GRAY + "Toggling this will make you vulnerable to attacks but also allow outgoing damage.",
                ChatColor.GRAY + "Display Item"}).build());

        // Stream Mode
        isToggled = gp.isStreamMode();
        inv.setItem(2, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, (short) (isToggled ? 10 : 8)), (isToggled ? ChatColor.GREEN : ChatColor.RED) + "Stream Mode", new String[]{
                ChatColor.GRAY + "Disable sensitive messages from being displayed.",
                ChatColor.GRAY + "Display Item"}).build());

        player.openInventory(inv);
    }

    /**
     * Opens the Head GM Toggles menu.
     * (user must be Head GM)
     *
     * @param player
     */
    public static void openHeadGameMasterTogglesMenu(Player player) {
        if (!Rank.isHeadGM(player)) return;
        boolean isToggled = false;

        Inventory inv = Bukkit.createInventory(null, 9, "Head Game Master Toggles");
        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (gp == null) return;

        // Game Master Extended Permissions
        isToggled = DungeonRealms.getInstance().isGMExtendedPermissions;
        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK, 1, (short) (isToggled ? 10 : 8)), (isToggled ? ChatColor.GREEN : ChatColor.RED) + "Game Master Extended Permissions", new String[]{
                ChatColor.GRAY + "Toggling this will allow GMs to have extended permissions.",
                ChatColor.GRAY + "This should be used for events and grants access to features such as adding items.",
                ChatColor.GRAY + "Display Item"}).build());

        player.openInventory(inv);
    }

}