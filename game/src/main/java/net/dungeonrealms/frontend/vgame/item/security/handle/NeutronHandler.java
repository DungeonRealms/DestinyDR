package net.dungeonrealms.frontend.vgame.item.security.handle;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import lombok.Getter;
import net.dungeonrealms.common.awt.frame.handler.Handler;
import net.dungeonrealms.frontend.Game;
import net.dungeonrealms.frontend.vgame.item.security.NUAIHolder;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import java.util.UUID;

/**
 * Created by Giovanni on 17-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class NeutronHandler implements Handler.ListeningHandler {

    @Getter
    private boolean prepared;

    @Getter
    private UUID uniqueId;

    @Override
    public void prepare() {
        Game.getGame().getServer().getPluginManager().registerEvents(this, Game.getGame());
        this.uniqueId = UUID.randomUUID();
        this.prepared = true;
    }

    @Override
    public void disable() {
        this.uniqueId = null;
        this.prepared = false;
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (NUAIHolder.getHolder().getAtomicList().get().contains(event.getItem().getItemStack())) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (NUAIHolder.getHolder().getAtomicList().get().contains(event.getItemDrop())) {
            event.setCancelled(true);
            event.getItemDrop().remove();
            event.getPlayer().updateInventory();
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (event.getPlayer().getItemInHand() != null) {
            if (NUAIHolder.getHolder().getAtomicList().get().contains(event.getPlayer().getItemInHand())) {
                event.setCancelled(true);
                event.getPlayer().getItemInHand().setType(Material.AIR);
                event.getPlayer().updateInventory();
            }
        }
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent event) {
        if (NUAIHolder.getHolder().getAtomicList().get().contains(event.getNewArmorPiece())) {
            event.setCancelled(true);
            event.getNewArmorPiece().setType(Material.AIR);
            event.getPlayer().updateInventory();
        }
    }
}
