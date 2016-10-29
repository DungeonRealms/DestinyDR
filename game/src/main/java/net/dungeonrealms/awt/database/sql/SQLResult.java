package net.dungeonrealms.awt.database.sql;

import java.util.logging.Level;

/**
 * Created by Matthew E on 10/29/2016 at 8:53 AM.
 */
public enum  SQLResult {

    FAILED(Level.SEVERE),
    SUCCESS(Level.INFO);

    private Level level;

    SQLResult(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }
}
