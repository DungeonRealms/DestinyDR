package net.dungeonrealms.frontend.vgame.player.teleportation;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.handler.Handler;
import net.dungeonrealms.frontend.Game;
import net.dungeonrealms.frontend.vgame.player.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class TeleportationHandler implements Handler.ListeningHandler {

    @Getter
    private boolean prepared;

    @Getter
    private UUID uniqueId;

    @Override
    public void prepare() {
        Game.getGame().getServer().getPluginManager().registerEvents(this, Game.getGame());
        this.prepared = true;
        this.uniqueId = UUID.randomUUID();
    }

    @Override
    public void disable() {
        this.uniqueId = null;
        this.prepared = false;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
                GamePlayer gamePlayer = Game.getGame().getRegistryRegistry().getPlayerRegistry().getPlayer(event.getEntity().getUniqueId());
                gamePlayer.getPlayer().sendMessage(ChatColor.RED + "Your teleport has been interrupted by combat!");
                gamePlayer.setTeleporting(false);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        GamePlayer gamePlayer = Game.getGame().getRegistryRegistry().getPlayerRegistry().getPlayer(event.getPlayer().getUniqueId());
        gamePlayer.getPlayer().sendMessage(ChatColor.RED + "Your teleport was cancelled!");
        gamePlayer.setTeleporting(false);
    }
}
