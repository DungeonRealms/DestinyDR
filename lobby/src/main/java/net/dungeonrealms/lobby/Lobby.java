package net.dungeonrealms.lobby;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.exception.ServerRunningException;
import net.dungeonrealms.common.awt.frame.server.lobby.LobbyCore;
import net.dungeonrealms.common.awt.frame.server.lobby.LobbyServer;

/**
 * Created by Giovanni on 5-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Lobby extends LobbyCore {

    @Getter
    private static Lobby lobby;

    @Override
    public void onEnable() {
        lobby = this;

        // Launch the maps for storage of lobby handlers
        this.preEnableMaps();

        // Start the actual game/lobby server
        try {
            this.enable(new LobbyServer(this));
        } catch (ServerRunningException e) {
            e.printStackTrace();
        }
    }
}
