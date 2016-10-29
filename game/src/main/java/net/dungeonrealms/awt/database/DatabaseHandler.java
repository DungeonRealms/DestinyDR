package net.dungeonrealms.awt.database;

import lombok.Getter;
import net.dungeonrealms.awt.SuperHandler;
import net.dungeonrealms.awt.database.sql.MySQL;
import net.dungeonrealms.common.Constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class DatabaseHandler implements SuperHandler.Handler {

    @Getter
    private ExecutorService pool = Executors.newFixedThreadPool(2);
    @Getter
    private MySQL mySQL;

    private boolean locked = false;

    @Override
    public void prepare() {
        //TODO connect via common-Constants
        mySQL = new MySQL(this, Constants.SQL_HOSTNAME, Constants.SQL_PORT, Constants.SQL_DATABASE,Constants.SQL_USERNAME, Constants.SQL_PASSWORD);
        if (!locked)
            locked = true;
    }
}
