package net.dungeonrealms.control.database;

import com.zaxxer.hikari.HikariDataSource;
import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.utils.UtilLogger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Evoltr on 11/15/2016.
 */
public class Database {

    private DRControl control;

    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    private HikariDataSource source;

    public Database(DRControl control) {
        this.control = control;

        this.host = control.getConfiguration().getSetting("db-host");
        this.port = control.getConfiguration().getSetting("db-port");
        this.database = control.getConfiguration().getSetting("db-database");
        this.username = control.getConfiguration().getSetting("db-username");
        this.password = control.getConfiguration().getSetting("db-password");
    }

    public void setup() {
        source = new HikariDataSource();

        source.setMaximumPoolSize(20);
        source.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        source.addDataSourceProperty("serverName", host);
        source.addDataSourceProperty("port", port);
        source.addDataSourceProperty("databaseName", database);
        source.addDataSourceProperty("user", username);
        source.addDataSourceProperty("password", password);

        UtilLogger.info("Connection established with the Database.");
    }

    public Connection getConnection() throws SQLException {
        return source.getConnection();
    }
}
