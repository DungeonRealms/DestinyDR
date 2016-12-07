package net.dungeonrealms.database.util;

/**
 * Created by Giovanni on 7-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Logger {

    public static void info(boolean prefix, String args) {
        if (prefix)
            System.out.print("INFO: " + args);
        else System.out.println(args);
    }
}
