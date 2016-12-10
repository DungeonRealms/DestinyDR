package net.dungeonrealms.database.exception;

/**
 * Created by Giovanni on 15-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ConnectionRunningException extends Exception {
    public ConnectionRunningException() {
        super("Double handle requested for a database net.dungeonrealms.database.connection");
    }
}
