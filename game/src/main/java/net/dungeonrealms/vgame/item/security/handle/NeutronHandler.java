package net.dungeonrealms.vgame.item.security.handle;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.awt.handler.old.SuperHandler;
import net.dungeonrealms.vgame.item.security.NUAIHolder;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * Created by Giovanni on 17-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class NeutronHandler implements SuperHandler.ListeningHandler {
    @Override
    public void prepare() {
        DungeonRealms.getInstance().getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
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
