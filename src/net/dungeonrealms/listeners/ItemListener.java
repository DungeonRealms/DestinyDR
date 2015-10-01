package net.dungeonrealms.listeners;

import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.teleportation.TeleportAPI;
import net.dungeonrealms.teleportation.Teleportation;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran on 9/18/2015.
 */
public class ItemListener implements Listener {
    /**
     * Used to stop player from dropping items that are
     * valuable e.g. hearthstone or profile head.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrop(PlayerDropItemEvent event) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getItemDrop().getItemStack());
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null || !tag.getString("type").equalsIgnoreCase("important")) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "[WARNING] " + ChatColor.YELLOW + "You can't drop important game items!");
    }

    /**
     * Handles player clicking with a teleportation item
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseTeleportItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.QUARTZ && player.getItemInHand().getType() != Material.BOOK)
            return;
        ItemStack itemStack = player.getItemInHand();
        if (!(CombatLog.isInCombat(event.getPlayer().getUniqueId()))) {
            if (TeleportAPI.isPlayerCurrentlyTeleporting(player.getUniqueId())) {
                player.sendMessage("You cannot restart a teleport during a cast!");
                return;
            }
            if (TeleportAPI.isHearthstone(itemStack)) {
                if (TeleportAPI.canUseHearthstone(player.getUniqueId())) {
                    net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
                    Teleportation.teleportPlayer(event.getPlayer().getUniqueId(), Teleportation.EnumTeleportType.HEARTHSTONE, nmsItem.getTag());
                } else {
                    player.sendMessage(
                            ChatColor.GREEN.toString() + ChatColor.BOLD + "HEARTHSTONE " + ChatColor.RED + "[Usage Exhausted] " + ChatColor.RED.toString() + "(" + ChatColor.UNDERLINE + TeleportAPI.getPlayerHearthstoneCD(player.getUniqueId()) + "s" + ChatColor.RED + ")");
                }
            } else if (TeleportAPI.isTeleportBook(itemStack)) {
                net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
                Teleportation.teleportPlayer(player.getUniqueId(), Teleportation.EnumTeleportType.TELEPORT_BOOK, nmsItem.getTag());
                if (player.getItemInHand().getAmount() == 1) {
                    player.setItemInHand(new ItemStack(Material.AIR));
                } else {
                    player.getItemInHand().setAmount((player.getItemInHand().getAmount() - 1));
                }
            } else {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "This item cannot be used to Teleport!");
            }
        } else {
            player.sendMessage(
                    ChatColor.GREEN.toString() + ChatColor.BOLD + "TELEPORT " + ChatColor.RED + "You are in combat! " + ChatColor.RED.toString() + "(" + ChatColor.UNDERLINE + CombatLog.COMBAT.get(player.getUniqueId()) + "s" + ChatColor.RED + ")");
        }
    }

    /**
     * Handles player clicking with their profile
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseProfileItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.SKULL_ITEM) return;
        ItemStack itemStack = player.getItemInHand();
        //TODO: Open player profile GUI.
    }
}
