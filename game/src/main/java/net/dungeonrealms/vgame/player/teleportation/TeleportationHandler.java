package net.dungeonrealms.vgame.player.teleportation;

import net.dungeonrealms.common.awt.handler.old.SuperHandler;
import net.dungeonrealms.vgame.old.Game;
import net.dungeonrealms.vgame.player.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class TeleportationHandler implements SuperHandler.ListeningHandler {

    @Override
    public void prepare() {
        Game.getGame().getServer().getPluginManager().registerEvents(this, Game.getGame());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
                GamePlayer gamePlayer = Game.getGame().getRegistryHandler().getPlayerRegistry().getMap().get(event.getEntity().getUniqueId());
                gamePlayer.getPlayer().sendMessage(ChatColor.RED + "Your teleport has been interrupted by combat!");
                gamePlayer.setTeleporting(false);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        GamePlayer gamePlayer = Game.getGame().getRegistryHandler().getPlayerRegistry().getMap().get(event.getPlayer().getUniqueId());
        gamePlayer.getPlayer().sendMessage(ChatColor.RED + "Your teleport was cancelled!");
        gamePlayer.setTeleporting(false);
    }
}
