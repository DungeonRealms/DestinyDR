package net.dungeonrealms.awt.events;

import net.dungeonrealms.awt.database.sql.MySQL;
import net.dungeonrealms.awt.database.sql.SQLResult;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Matthew E on 10/29/2016 at 8:50 AM.
 */
public class DatabaseConnectEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private MySQL mySQL;
    private SQLResult sqlResult;

    public DatabaseConnectEvent(boolean async, MySQL mySQL, SQLResult sqlResult) {
        super(async);
        this.mySQL = mySQL;
        this.sqlResult = sqlResult;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public SQLResult getSQLResult() {
        return sqlResult;
    }
}
