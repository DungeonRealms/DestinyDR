package net.dungeonrealms.game.player.altars;

import net.dungeonrealms.common.game.util.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;

/**
 * Created by Rar349 on 8/3/2017.
 */
public class AltarListener implements Listener {

    @EventHandler
    public void onItemThrow(PlayerDropItemEvent event) {
        Block block = event.getPlayer().getTargetBlock((Set)null,5);
        if(block == null) return;
        Altars altar = AltarManager.getAltarFromLocation(block.getLocation());
        if(altar == null) return;
        Location node = altar.getNode(block);
        if(node == null) return;
        int nodeIndex = altar.getNodeIndex(node);
        Altar current = AltarManager.getAltar(altar);
        if(current == null && event.getPlayer().getOpenInventory().getType() != InventoryType.CHEST) {
            Item item = event.getItemDrop();
            current = new Altar(event.getPlayer(), altar);
            AltarManager.currentlyUsingAltars.put(altar, current);
            System.out.println("NODE INDEX IS: " + nodeIndex);
            current.setItemStack(nodeIndex, item.getItemStack());
            item.remove();
            return;
        }
        if(current != null && event.getPlayer().getOpenInventory().getType() != InventoryType.CHEST) {
            if (current.getUsing() != event.getPlayer()) {
                event.getPlayer().sendMessage(ChatColor.RED + "Someone is already using this altar!");
                event.setCancelled(true);
                return;
            }

            if (current.hasActiveStack(nodeIndex)) {
                event.getPlayer().sendMessage(ChatColor.RED + "There is already an item on this pedestal!");
                event.setCancelled(true);
                return;
            }

            if (current.getItemStack(0) != null) {
                event.getPlayer().sendMessage(ChatColor.RED + "This altar is currently charging!");
                event.setCancelled(true);
                return;
            }

            current.setItemStack(nodeIndex, event.getItemDrop().getItemStack());
            event.getItemDrop().remove();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(event.getTo().getBlock().getLocation().equals(event.getFrom().getBlock().getLocation())) return;
        Altar altar = AltarManager.getAltar(event.getPlayer());
        if(altar == null) return;
        if(!altar.getAltarType().getWorld().equals(event.getPlayer().getWorld()) || altar.getAltarType().getCenterLocation().distanceSquared(event.getTo()) > 200) {
            AltarManager.removeAltar(altar, true);
        }
    }
}
