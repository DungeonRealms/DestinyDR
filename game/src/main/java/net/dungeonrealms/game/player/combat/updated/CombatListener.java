package net.dungeonrealms.game.player.combat.updated;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * Created by Giovanni on 24-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CombatListener implements Listener {

    public void start() {
        DungeonRealms.getInstance().getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if(event.getDamager() instanceof Player) {
            if(event.getEntity() instanceof Zombie) {
                Zombie zombie = (Zombie) event.getEntity();
                if(CombatAPI.getInstance().getOf(zombie) != null) {
                    CombatAPI.getInstance().getOf(zombie).handleDeath(zombie.getLocation());
                    CombatEntity combatEntity = CombatAPI.getInstance().getOf(zombie);
                    CombatAPI.getInstance().setLogged(combatEntity.getOwner());
                    CombatAPI.getInstance().despawnEntity(combatEntity);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        if (CombatAPI.getInstance().isTagged(event.getPlayer())) {
            // Spawn a logger if a player is combat tagged & leaves
            CombatAPI.getInstance().spawnEntity(new CombatEntity(event.getPlayer()));
            event.getPlayer().getLocation().getWorld().strikeLightning(event.getPlayer().getLocation());

            DatabaseAPI.getInstance().update(event.getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.IS_COMBAT_LOGGED, true, true, null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        CombatEntity combatEntity = CombatAPI.getInstance().getOf(event.getPlayer());
        // Player joins back but the combat entity NPC is not despawned yet
        if (combatEntity != null) {
            // Player joined back in time, let him keep his items
            event.getPlayer().teleport(combatEntity.getLoggerEntity());
            CombatAPI.getInstance().despawnEntity(combatEntity);
            DungeonRealms.getInstance().getServer().getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> event.getPlayer().sendMessage(ChatColor.RED + "You've been combat logged - Luckily you joined back"), 20 * 2);
        } else {
            // Player joins back, but is combat logged but the combat entity is despawned already
            if (CombatAPI.getInstance().isLiveLogged(event.getPlayer())) {
                event.getPlayer().getInventory().clear();
                DungeonRealms.getInstance().getServer().getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> event.getPlayer().sendMessage(ChatColor.RED + "You've been combat logged - Some items have been lost"), 20 * 2);
            } else {
                // Player joins back, but the server session has no trace of a combat log, check the database
                boolean logged = (boolean) DatabaseAPI.getInstance().getData(EnumData.IS_COMBAT_LOGGED, event.getPlayer().getUniqueId());
                if (logged) {
                    // Oh boy..
                    DungeonRealms.getInstance().getServer().getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> event.getPlayer().sendMessage(ChatColor.RED + "Some items have been lost"), 20 * 2);
                } else {
                    event.getPlayer().sendMessage("debug");
                }
            }
        }
        // Always update database
        DatabaseAPI.getInstance().update(event.getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.IS_COMBAT_LOGGED, false, true, null);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event) {
        // Don't allow teleportation in combat
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND) return;
        if (CombatAPI.getInstance().isTagged(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You can't teleport whilst in combat!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        // Don't allow command sending in combat
        if (!CombatAPI.getInstance().isTagged(event.getPlayer())) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "You can't use commands whilst in combat!");

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        // Prevent chunk deadlock
        for (Entity ent : event.getChunk().getEntities())
            if (ent instanceof Zombie && CombatAPI.getInstance().getOf((Zombie) ent) != null) ent.remove();
    }
}
