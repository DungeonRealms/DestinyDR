package net.dungeonrealms.database.api;

import net.dungeonrealms.database.Database;
import net.dungeonrealms.database.api.player.DataPlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 10-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PlayerConnection {

    private Database database;

    private ConcurrentHashMap<UUID, DataPlayer> dataPlayerCache;

    public PlayerConnection(Database database) {
        this.database = database;
        this.dataPlayerCache = new ConcurrentHashMap<>();
    }
}
