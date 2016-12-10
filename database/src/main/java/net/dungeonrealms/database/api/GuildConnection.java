package net.dungeonrealms.database.api;

import net.dungeonrealms.database.Database;

/**
 * Created by Giovanni on 10-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GuildConnection {

    private Database database;

    public GuildConnection(Database database) {
        this.database = database;
    }
}
