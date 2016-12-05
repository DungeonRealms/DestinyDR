package net.dungeonrealms.common.awt.database.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Giovanni on 15-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class MySQL {
    public Connection connection;

    private final String user;
    private final String database;
    private final String password;
    private final String port;
    private final String hostname;

    public MySQL(String hostname, String port, String database, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = username;
        this.password = password;
    }

    public boolean checkConnection() throws SQLException {
        return connection != null && !connection.isClosed();
    }

    public void updateConnection() throws SQLException, ClassNotFoundException {
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

    public ResultSet query(String query) throws SQLException, ClassNotFoundException {
        updateConnection();
        return connection.createStatement().executeQuery(query);
    }

    public int update(String query) throws SQLException, ClassNotFoundException {
        updateConnection();
        return connection.createStatement().executeUpdate(query);
    }

    public Connection openConnection() throws SQLException, ClassNotFoundException {
        if (checkConnection()) {
            return connection;
        }
        connection = DriverManager.getConnection(
                "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database, this.user, this.password);
        return connection;
    }
}
