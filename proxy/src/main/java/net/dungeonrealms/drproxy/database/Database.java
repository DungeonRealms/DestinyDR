package net.dungeonrealms.drproxy.database;

import com.zaxxer.hikari.HikariDataSource;
import net.dungeonrealms.drproxy.DRProxy;
import net.dungeonrealms.drproxy.player.NetworkPlayer;
import net.dungeonrealms.drproxy.player.rank.Rank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Evoltr on 11/30/2016.
 */
public class Database {

    private DRProxy plugin;

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    private HikariDataSource source;

    public Database(DRProxy plugin) {
        this.plugin = plugin;

        this.host = plugin.getConfig().getString("db.host");
        this.port = plugin.getConfig().getInt("db.port");
        this.database = plugin.getConfig().getString("db.database");
        this.username = plugin.getConfig().getString("db.username");
        this.password = plugin.getConfig().getString("db.password");
    }

    public void setup() {
        source = new HikariDataSource();
        source.setMaximumPoolSize(5);
        source.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        source.addDataSourceProperty("serverName", host);
        source.addDataSourceProperty("port", port);
        source.addDataSourceProperty("databaseName", database);
        source.addDataSourceProperty("user", username);
        source.addDataSourceProperty("password", password);
    }

    public Connection getConnection() throws SQLException {
        return source.getConnection();
    }

    public void createPlayer(String uuid, String name, String ip) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = source.getConnection();

            ps = connection.prepareStatement("INSERT INTO players (`uuid`, `name`, `ip`) VALUES(?, ?, ?)" +
                    "ON DUPLICATE KEY UPDATE `name`=?, `ip`=?, `last_seen`=NOW()");

            ps.setString(1, uuid);
            ps.setString(2, name);
            ps.setString(3, ip);
            ps.setString(4, name);
            ps.setString(5, ip);

            ps.execute();
        } finally {
            if (ps != null) ps.close();

            close(connection);
        }
    }

    public NetworkPlayer loadPlayer(String uuid) throws SQLException {
        Connection connection = null;

        PreparedStatement ps = null;
        ResultSet set = null;

        try {
            connection = source.getConnection();

            ps = connection.prepareStatement("SELECT * FROM `players` WHERE `uuid`=?");
            ps.setString(1, uuid);

            set = ps.executeQuery();

            if (set.next()) {
                String name = set.getString("name");
                Rank rank = Rank.getRank(set.getString("rank"));

                return new NetworkPlayer(uuid, name, rank);
            }

            return null;
        } finally {
            if (set != null) set.close();
            if (ps != null) ps.close();

            close(connection);
        }
    }


    public void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
