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
        if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.QUARTZ) return;

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(player.getItemInHand());
        NBTTagCompound tag = nmsItem.getTag();
        if (nmsItem == null || tag == null) return;
        if (!(tag.getString("type").equalsIgnoreCase("important") && tag.getString("usage").equalsIgnoreCase("hearthstone"))) return;
        if (TeleportAPI.canUseHearthstone(player.getUniqueId())) {
            if (!(CombatLog.isInCombat(event.getPlayer().getUniqueId()))) {
                Teleportation.teleportPlayer(event.getPlayer().getUniqueId());
            } else {
                player.sendMessage(
                        ChatColor.GREEN.toString() + ChatColor.BOLD + "HEARTHSTONE " + ChatColor.RED + "You are in combat! " + ChatColor.RED.toString() + "(" + ChatColor.UNDERLINE + TeleportAPI.getPlayerHearthstoneCD(player.getUniqueId()) + "s" + ChatColor.RED + ")");
            }
        } else {
            player.sendMessage(
                    ChatColor.GREEN.toString() + ChatColor.BOLD + "HEARTHSTONE " + ChatColor.RED + "[Usage Exhausted] " + ChatColor.RED.toString() + "(" + ChatColor.UNDERLINE + TeleportAPI.getPlayerHearthstoneCD(player.getUniqueId()) + "s" + ChatColor.RED + ")");
        }
    }
}
