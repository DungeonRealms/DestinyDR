package net.dungeonrealms.common.awt.frame.server.lobby;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.handler.HandlerMap;
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
    private HandlerMap handlerMap;

    @Getter
    private String lobbyId;

    @Getter
    private transient boolean connectionsAllowed = false;

    @Getter
    private MongoConnection mongoConnection;

    public LobbyServer(LobbyCore lobbyCore) {
        this.core = lobbyCore;
    }

    public void start() {
        if (!this.connectionsAllowed) {
            // Init connection to the mongo
            this.mongoConnection = new MongoConnection();
            try {
                this.mongoConnection.runOn("", "dungeonrealms");
            } catch (ConnectionRunningException e) {
                // This will never happen
            }
            this.connectionsAllowed = this.core.isPrepared();
        }
    }

    /**
     * Enable all maps
     */
    protected void preEnableMaps() {
        this.handlerMap = new HandlerMap();
    }
}
