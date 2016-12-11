package net.dungeonrealms.database.api;

import net.dungeonrealms.database.Database;
import net.dungeonrealms.database.lib.DataPipeline;

/**
 * Created by Giovanni on 10-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GuildConnection extends DataPipeline {

    private Database database;

    public GuildConnection(Database database) {
        this.database = database;
    }

    @Override @Deprecated
    protected void handleConnection(Object object) {
        // Do nothing
    }

    // TODO

}
