package net.dungeonrealms.backend.pipeline;

import net.dungeonrealms.database.api.player.DataPlayer;
import net.dungeonrealms.database.lib.PipelineHandler;
import net.dungeonrealms.frontend.Game;
import net.dungeonrealms.frontend.vgame.player.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Created by Giovanni on 11-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PlayerPipeline extends PipelineHandler {

    @Override
    public void start() {
        Game.getGame().getServer().getPluginManager().registerEvents(this, Game.getGame());
    }

    @EventHandler
    public void onConnect(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        // Request the data
        DataPlayer dataPlayer = Game.getGame().getDatabase().getApi().players().requestData(uuid).getIfExists(uuid);
        if (dataPlayer != null) {
            GamePlayer gamePlayer = new GamePlayer(dataPlayer);
            // Accept their connection
            Game.getGame().getRegistryRegistry().getPlayerRegistry().acceptConnection(gamePlayer);
        } else {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Failed to load your data, please reconnect");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        if (!Game.getGame().getRegistryRegistry().getPlayerRegistry().isAccepted(player.getUniqueId())) {
            player.kickPlayer(ChatColor.RED + "Failed to load your data, please reconnect");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Player player = event.getPlayer();
        if (Game.getGame().getDatabase().getApi().players().getIfExists(player.getUniqueId()) != null) {
            // Save their data
            Game.getGame().getDatabase().getApi().players().saveData(player.getUniqueId());
            // Remove their data
            Game.getGame().getDatabase().getApi().players().removeData(player.getUniqueId());
            // Remove their player model
            GamePlayer gamePlayer = Game.getGame().getRegistryRegistry().getPlayerRegistry().getPlayer(player.getUniqueId());
            Game.getGame().getRegistryRegistry().getPlayerRegistry().removePlayer(gamePlayer);
            // Finished
        }
    }
}
