package net.dungeonrealms.awt.database.sql;

import lombok.Getter;
import net.dungeonrealms.awt.database.DatabaseHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Giovanni on 8-7-2016.
 */
public class MySQL {
    public Connection connection;

    private final String user;
    @Getter
    private final String database;
    private final String password;
    @Getter
    private final int port;
    @Getter
    private final String hostname;
    @Getter
    private DatabaseHandler databaseHandler;

    public MySQL(DatabaseHandler databaseHandler, String hostname, int port, String database, String username, String password) {
        this.databaseHandler = databaseHandler;
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = username;
        this.password = password;
    }

    public boolean checkConnection() throws SQLException {
        return connection != null && !connection.isClosed();
    }

    public void updateConnection() throws SQLException {
        if (!checkConnection())
            openConnection();
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() throws SQLException {
        if (connection != null)
            connection.close();
    }

    public ResultSet query(String query) throws SQLException {
        databaseHandler.getPool().submit(() -> {
            updateConnection();
            return connection.createStatement().executeQuery(query);
        });
        return null;
    }

    public int update(String query) throws SQLException {
        updateConnection();
        databaseHandler.getPool().submit(() -> {
            return connection.createStatement().executeUpdate(query);
        });
        return -1;
    }

    public Connection openConnection() throws SQLException {
        databaseHandler.getPool().submit(() -> {
            if (checkConnection()) {
                return connection;
            }
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database, this.user, this.password);
            return connection;
        });
        return null;
    }
}
