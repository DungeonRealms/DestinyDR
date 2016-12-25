package net.dungeonrealms.game.listener.inventory;

import com.google.common.collect.Sets;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.Constants;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;

import java.util.Set;
import java.util.UUID;

/**
 * Created by Giovanni on 25-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class InventorySecurity implements Listener {

    /**
     * Fixes players interacting whilst having inventory open
     * <p>
     * - Biggest dupe was because of players doing stuff whilst having an inventory open
     */

    @Getter
    private Set<UUID> inventoryViewers;

    private String kickMessage = ChatColor.RED + "Invalid inventory ID";


    public void start() {
        DungeonRealms.getInstance().getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        this.inventoryViewers = Sets.newHashSet();

        DungeonRealms.getInstance().getServer().getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            Bukkit.getOnlinePlayers().stream().filter(player -> this.inventoryViewers.contains(player.getUniqueId()) && player.getOpenInventory() == null).forEach(player -> {
                this.inventoryViewers.remove(player.getUniqueId());
            });
        }, 0L, 15);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamageInInventory(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (this.inventoryViewers.contains(player.getUniqueId())) {
                player.closeInventory();
            } else {
                if (player.getOpenInventory() != null) {
                    Constants.log.warning("PLAYER OUT OF INVENTORY BOUNDS, KICKED");
                    player.closeInventory();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        // If a player does NOT open his own inventory, check him
        if (event.getInventory().getType() != InventoryType.PLAYER) {
            // A player opens a shop/bank or whatever
            if (!this.inventoryViewers.contains(event.getPlayer().getUniqueId())) {
                this.inventoryViewers.add(event.getPlayer().getUniqueId());
            } else {
                Constants.log.warning(event.getEventName() + " PLAYER OUT OF INVENTORY BOUNDS, KICKED");
                // Suspicious, the player is opening an inventory whilst being in the inventory viewers set
                event.setCancelled(true);
                this.inventoryViewers.remove(event.getPlayer().getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (this.inventoryViewers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (this.inventoryViewers.contains(event.getPlayer().getUniqueId())) {
            this.inventoryViewers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (this.inventoryViewers.contains(event.getPlayer().getUniqueId())) {
            // A player is chatting whilst viewing an inventory, kick.
            Constants.log.warning(event.getEventName() + " PLAYER OUT OF INVENTORY BOUNDS, KICKED");
            event.setCancelled(true);
            event.getPlayer().kickPlayer(this.kickMessage);
            this.inventoryViewers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        if (this.inventoryViewers.contains(event.getPlayer().getUniqueId())) {
            // A player is moving whilst viewing an inventory, kick
            Constants.log.warning(event.getEventName() + " PLAYER OUT OF INVENTORY BOUNDS, KICKED");
            event.setCancelled(true);
            this.inventoryViewers.remove(event.getPlayer().getUniqueId());
            event.getPlayer().kickPlayer(this.kickMessage);
        } else {
            if (event.getPlayer().getOpenInventory() != null) {
                event.getPlayer().closeInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event) {
        if (this.inventoryViewers.contains(event.getPlayer().getUniqueId())) {
            // A player is teleporting whilst viewing an inventory, kick
            Constants.log.warning(event.getEventName() + " PLAYER OUT OF INVENTORY BOUNDS, KICKED");
            event.setCancelled(true);
            event.getPlayer().kickPlayer(this.kickMessage);
            this.inventoryViewers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (this.inventoryViewers.contains(event.getPlayer().getUniqueId())) {
            // A player is changing world whilst viewing an inventory, kick
            Constants.log.warning(event.getEventName() + " PLAYER OUT OF INVENTORY BOUNDS, KICKED");
            event.getPlayer().kickPlayer(this.kickMessage);
            this.inventoryViewers.remove(event.getPlayer().getUniqueId());
        }
    }
}
