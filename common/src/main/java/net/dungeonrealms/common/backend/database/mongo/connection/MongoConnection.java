package net.dungeonrealms.common.backend.database.mongo.connection;

import lombok.Getter;
import net.dungeonrealms.common.backend.database.connection.exception.ConnectionRunningException;
import net.dungeonrealms.common.backend.database.mongo.Mongo;
import net.dungeonrealms.common.backend.database.mongo.MongoAPI;

/**
 * Created by Giovanni on 18-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class MongoConnection
{
    @Getter
    private Mongo mongo;

    @Getter
    private MongoAPI api;

    private boolean running = false;

    public void runOn(String URI, String fromDatabase) throws ConnectionRunningException
    {
        if (!running)
        {
            this.mongo = new Mongo(URI, fromDatabase);
            this.api = new MongoAPI(this.mongo);
        } else
            throw new ConnectionRunningException();
    }
}
