package net.dungeonrealms.common.awt.frame.server.lobby;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.server.IServer;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Giovanni on 5-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class LobbyCore extends JavaPlugin implements IServer {

    @Getter
    private JavaPlugin access;

    @Getter
    private boolean prepared;

    @Getter
    private LobbyServer lobbyServer;

    /**
     * Start the lobby server
     */
    protected void enable(LobbyServer lobbyServer) {
        if (lobbyServer != null && lobbyServer.isConnectionsAllowed()) {
            this.access = this;

            this.prepared = true;
        }
    }
}
