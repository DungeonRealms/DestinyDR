package net.dungeonrealms.awt.database;

import lombok.Getter;
import net.dungeonrealms.awt.SuperHandler;
import net.dungeonrealms.awt.database.direct.SQLDatabase;
import net.dungeonrealms.awt.database.sql.MySQL;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class DatabaseHandler implements SuperHandler.Handler
{
    @Getter
    private SQLDatabase sqlDatabase;

    private boolean locked = false;

    @Override
    public void prepare()
    {
        if (!this.locked)
        {
            initConnection();
            this.locked = true;
        }
    }

    protected void initConnection()
    {
        this.sqlDatabase = new SQLDatabase(null, null, null, null, null); //TODO
    }
}
