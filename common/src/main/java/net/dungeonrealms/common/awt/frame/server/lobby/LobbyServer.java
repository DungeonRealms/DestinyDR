package net.dungeonrealms.common.awt.frame.server.lobby;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.awt.database.connection.exception.ConnectionRunningException;
import net.dungeonrealms.common.awt.database.mongo.connection.MongoConnection;

/**
 * Created by Giovanni on 5-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class LobbyServer {

    @Getter
    private LobbyCore core;

    @Getter
    private String lobbyId;

    @Getter
    @Setter
    private boolean enabled;


    public LobbyServer(LobbyCore lobbyCore) {
        this.core = lobbyCore;
    }

    public void start() {
        if (!this.enabled) {
            this.enabled = true;
        }
    }
}
