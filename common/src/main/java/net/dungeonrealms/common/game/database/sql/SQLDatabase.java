package net.dungeonrealms.common.game.database.sql;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class SQLDatabase {

    private String url, host, username, password, database;

//    public SQLDatabase() {
//        this(DungeonRealms.getInstance().getConfig().getString("sql.hostname"), AdPoints.get().getConfig().getString("sql.username"), AdPoints.get()
//                .getConfig().getString("sql.password"), AdPoints.get().getConfig().getString("sql.database"));
//    }

    public SQLDatabase(String host, String username, String password, String database) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.database = database;
        this.url = "jdbc:mysql://" + host + ":3306/" + database;
    }

    protected Connection connection;

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

            //Create the properties so we can get everything in order.
            Properties props = new Properties();
            props.setProperty("password", this.password);
            props.setProperty("user", this.username);
            props.setProperty("database", this.database);
//            props.setProperty("connectTimeout", "3000");
            props.setProperty("autoReconnect", "true");
            props.setProperty("continueBatchOnError", "true");
            props.setProperty("rewriteBatchStatements", "true");
//            connection = DriverManager.getConnection(url, props);
            connection = DriverManager.getConnection(url, username, password);
            Bukkit.getLogger().info("Connection created..");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

}
