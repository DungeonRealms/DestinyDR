package net.dungeonrealms.common.game.database.sql;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLDatabase {

    private String url, host, username, password, database;

//    public SQLDatabase() {
//        this(DungeonRealms.getInstance().getConfig().getString("sql.hostname"), AdPoints.get().getConfig().getString("sql.username"), AdPoints.get()
//                .getConfig().getString("sql.password"), AdPoints.get().getConfig().getString("sql.database"));
//    }

    public SQLDatabase(String host, String username, String password, String database) {
        this.username = username;
        this.password = password;
        this.database = database;
        this.url = "jdbc:mysql://" + host + ":3306/" + database + "?autoReconnect=true";
    }

    protected static Connection connection;

    public boolean isConnected() {
        try {
            Connection connect = getConnection();
            return connect != null;
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized Connection getConnection() {
        try {
            if (connection != null) {
                if (connection.isValid(1)) {
                    return connection;
                } else {
                    connection.close();
                }
            }
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

}
