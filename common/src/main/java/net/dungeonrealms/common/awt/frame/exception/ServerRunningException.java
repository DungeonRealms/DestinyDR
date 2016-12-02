package net.dungeonrealms.common.awt.frame.exception;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ServerRunningException extends Exception {

    public ServerRunningException() {
        super("This Dungeon Realms shard instance is already running");
    }
}
