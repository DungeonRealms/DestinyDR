package net.dungeonrealms.game.listener.inventory;

import com.google.common.collect.Sets;
import lombok.Getter;
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
        this.inventoryViewers = Sets.newHashSet();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        // If a player does NOT open his own inventory, check him
        if (event.getInventory().getType() != InventoryType.PLAYER) {
            // A player opens a shop/bank or whatever
            if (!this.inventoryViewers.contains(event.getPlayer().getUniqueId())) {
                this.inventoryViewers.add(event.getPlayer().getUniqueId());
            } else {
                // Suspicious, the player is opening an inventory whilst being in the inventory viewers set
                event.setCancelled(true);
                ((Player) event.getPlayer()).kickPlayer(this.kickMessage);
                this.inventoryViewers.remove(event.getPlayer().getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickup(PlayerPickupItemEvent event) {
        if(this.inventoryViewers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (this.inventoryViewers.contains(event.getPlayer().getUniqueId())) {
            this.inventoryViewers.remove(event.getPlayer().getUniqueId());
        } else {
            // Suspicious, a player is closing an inventory without being logged
            ((Player) event.getPlayer()).kickPlayer(this.kickMessage);
            this.inventoryViewers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (this.inventoryViewers.contains(event.getPlayer().getUniqueId())) {
            // A player is chatting whilst viewing an inventory, kick.
            event.setCancelled(true);
            event.getPlayer().kickPlayer(this.kickMessage);
            this.inventoryViewers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(PlayerMoveEvent event) {
        if (this.inventoryViewers.contains(event.getPlayer().getUniqueId())) {
            // A player is moving whilst viewing an inventory, kick
            event.setCancelled(true);
            this.inventoryViewers.remove(event.getPlayer().getUniqueId());
            event.getPlayer().kickPlayer(this.kickMessage);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event) {
        if (this.inventoryViewers.contains(event.getPlayer().getUniqueId())) {
            // A player is teleporting whilst viewing an inventory, kick
            event.setCancelled(true);
            event.getPlayer().kickPlayer(this.kickMessage);
            this.inventoryViewers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (this.inventoryViewers.contains(event.getPlayer().getUniqueId())) {
            // A player is changing world whilst viewing an inventory, kick
            event.getPlayer().kickPlayer(this.kickMessage);
            this.inventoryViewers.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamage() != 0) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (this.inventoryViewers.contains(player.getUniqueId())) {
                    // Uh oh..
                    player.kickPlayer(this.kickMessage);
                    this.inventoryViewers.remove(player.getUniqueId());
                }
            }
        }
    }
}
