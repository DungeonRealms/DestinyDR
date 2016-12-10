package net.dungeonrealms.frontend.vgame.player.connect;

import lombok.Getter;
import net.dungeonrealms.common.awt.data.DataPlayer;
import net.dungeonrealms.common.awt.database.connection.IConnection;
import net.dungeonrealms.frontend.Game;
import net.dungeonrealms.frontend.vgame.player.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Created by Giovanni on 3-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PlayerConnection implements IConnection {

    @Getter
    private UUID uniqueId;

    @Getter
    private boolean prepared;

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

    @Override
    public boolean allowSynchronized() {
        return false;
    }

    @EventHandler
    public void onAsyncLogin(AsyncPlayerPreLoginEvent event) {
        // Request the player's data
        Game.getGame().getGameShard().getMongoConnection().getApi().requestPlayerData(event.getUniqueId());
        // Check if it's accepted
        if (!Game.getGame().getGameShard().getMongoConnection().getApi().exists(event.getUniqueId())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Invalid data model, please reconnect");
        } else {
            // Accept their net.dungeonrealms.database.connection and add them to the online player map
            DataPlayer dataPlayer = Game.getGame().getGameShard().getMongoConnection().getApi().getPlayer(event.getUniqueId());
            Game.getGame().getRegistryRegistry().getPlayerRegistry().acceptConnection(new GamePlayer(dataPlayer));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        // Second check -> check if the player's data actually exists
        if (!Game.getGame().getGameShard().getMongoConnection().getApi().exists(event.getPlayer().getUniqueId())
                || !Game.getGame().getRegistryRegistry().getPlayerRegistry().isAccepted(event.getPlayer().getUniqueId())) {
            event.getPlayer().kickPlayer(ChatColor.RED + "Invalid data model, please reconnect");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        // Check if the player's gameplayer/data actually exists
        if (Game.getGame().getRegistryRegistry().getPlayerRegistry().isAccepted(event.getPlayer().getUniqueId())
                && Game.getGame().getGameShard().getMongoConnection().getApi().exists(event.getPlayer().getUniqueId())) {
            GamePlayer gamePlayer = Game.getGame().getRegistryRegistry().getPlayerRegistry().getPlayer(event.getPlayer().getUniqueId());
            // Save the data first
            Game.getGame().getGameShard().getMongoConnection().getApi().saveDataPlayer(gamePlayer.getPlayer().getUniqueId(), true);
            // Now safely disconnect the player
            Game.getGame().getRegistryRegistry().getPlayerRegistry().removePlayer(gamePlayer);
        }
    }
}
