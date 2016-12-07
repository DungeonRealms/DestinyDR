package net.dungeonrealms.lobby.player.connection;

import lombok.Getter;
import net.dungeonrealms.common.awt.data.DataPlayer;
import net.dungeonrealms.common.awt.database.connection.IConnection;
import net.dungeonrealms.lobby.Lobby;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

/**
 * Created by Giovanni on 5-12-2016.
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
        this.uniqueId = UUID.randomUUID();
        Lobby.getLobby().getServer().getPluginManager().registerEvents(this, Lobby.getLobby());
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
        UUID uuid = event.getUniqueId();
        // Request the player data & send it to the server
        Lobby.getLobby().getLobbyServer().getMongoConnection().getApi().requestPlayerData(uuid);
        if (Lobby.getLobby().getLobbyServer().getMongoConnection().getApi().exists(uuid)) {
            DataPlayer dataPlayer = Lobby.getLobby().getLobbyServer().getMongoConnection().getApi().getPlayer(uuid);
            // TODO send packet to master server containing raw data
        } else {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Invalid data model, please reconnect");
        }
    }
}
