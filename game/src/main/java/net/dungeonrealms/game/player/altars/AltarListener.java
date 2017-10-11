package net.dungeonrealms.game.player.altars;

import net.dungeonrealms.common.game.util.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
        System.out.println("Throw item debug 1");
        Block block = event.getPlayer().getTargetBlock((Set)null,5);
        if(block == null) return;
        System.out.println("Throw item debug 2: " + block.getType());
        System.out.println("Throw item debug 22: " + block.getLocation());
        Altars altar = AltarManager.getAltarFromLocation(block.getLocation());
        if(altar == null) return;
        System.out.println("Throw item debug 3");
        Location node = altar.getNode(block);
        if(node == null) return;
        System.out.println("Throw item debug 4");
        int nodeIndex = altar.getNodeIndex(node);
        Altar current = AltarManager.getAltar(altar);
        if(current == null) {
            System.out.println("Throw item debug 5: " + altar);
            Item item = event.getItemDrop();
            current = new Altar(event.getPlayer(), altar);
            AltarManager.currentlyUsingAltars.put(altar, current);
            current.setItemStack(nodeIndex, item.getItemStack());
            item.remove();
            return;
        }
        if(current.getUsing() != event.getPlayer()) {
            System.out.println("Throw item debug 6");
            event.getPlayer().sendMessage(ChatColor.RED + "Someone is already using this altar!");
            event.setCancelled(true);
            return;
        }

        if(current.hasActiveStack(nodeIndex)) {
            System.out.println("Throw item debug 7");
            event.getPlayer().sendMessage(ChatColor.RED + "There is already an item on this pedestal!");
            event.setCancelled(true);
            return;
        }

        if(current.getItemStack(0) != null) {
            System.out.println("Throw item debug 8");
            event.getPlayer().sendMessage(ChatColor.RED + "This altar is currently charging!");
            event.setCancelled(true);
            return;
        }

        System.out.println("Throw item debug 9");

        current.setItemStack(nodeIndex,event.getItemDrop().getItemStack());
        event.getItemDrop().remove();
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
