package net.dungeonrealms.common.awt.frame.server.shard;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.awt.database.connection.exception.ConnectionRunningException;
import net.dungeonrealms.common.awt.database.mongo.connection.MongoConnection;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GameShard {

    @Getter
    private ServerCore serverCore;

    @Getter
    @Setter
    private boolean enabled;

    @Getter
    private MongoConnection mongoConnection;

    public GameShard(ServerCore serverCore) {
        this.serverCore = serverCore;
    }

    public void start() {
        if(!enabled) {
            // Init mongo connection
            this.mongoConnection = new MongoConnection();
            try {
                this.mongoConnection.runOn("", "dungeonrealms");
            } catch (ConnectionRunningException e) {
                // This will never happen
            }
            this.enabled = true;
        }
    }
}
