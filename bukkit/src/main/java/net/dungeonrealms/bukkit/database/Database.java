package net.dungeonrealms.bukkit.database;

import com.zaxxer.hikari.HikariDataSource;
import net.dungeonrealms.bukkit.BukkitCore;
import net.dungeonrealms.bukkit.player.NetworkPlayer;
import net.dungeonrealms.bukkit.player.rank.Rank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Evoltr on 12/7/2016.
 */
public class Database {

    private BukkitCore plugin;

    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    private HikariDataSource source;

    public Database(BukkitCore plugin) {
        this.plugin = plugin;

        this.host = plugin.getConfig().getString("db.host");
        this.port = plugin.getConfig().getString("db.port");
        this.database = plugin.getConfig().getString("db.database");
        this.username = plugin.getConfig().getString("db.username");
        this.password = plugin.getConfig().getString("db.password");
    }

    public void setup() {
        source = new HikariDataSource();
        source.setMaximumPoolSize(10);
        source.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        source.addDataSourceProperty("serverName", host);
        source.addDataSourceProperty("port", port);
        source.addDataSourceProperty("databaseName", database);
        source.addDataSourceProperty("user", username);
        source.addDataSourceProperty("password", password);
    }

    public void shutdown() {
        try {
            source.shutdown();
        } catch (Exception e) {
            //Failed.
        }
    }

    public Connection getConnection() throws SQLException {
        return source.getConnection();
    }

    public NetworkPlayer loadPlayerByName(String name) throws SQLException {
        Connection connection = null;

        PreparedStatement ps = null;
        ResultSet set = null;

        try {
            connection = source.getConnection();

            ps = connection.prepareStatement("SELECT * FROM `players` WHERE `name`=? ORDER BY `last_seen` DESC");
            ps.setString(1, name);

            set = ps.executeQuery();

            if (set.next()) {
                String uuid = set.getString("uuid");
                String ip = set.getString("ip");
                Rank rank = Rank.getRank(set.getString("rank"));

                return new NetworkPlayer(uuid, name, ip, rank);
            }

            return null;
        } finally {
            if (set != null) set.close();
            if (ps != null) ps.close();

            close(connection);
        }
    }

    public NetworkPlayer loadPlayerByUUID(String uuid) throws SQLException {
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
                String ip = set.getString("ip");
                Rank rank = Rank.getRank(set.getString("rank"));

                return new NetworkPlayer(uuid, name, ip, rank);
            }

            return null;
        } finally {
            if (set != null) set.close();
            if (ps != null) ps.close();

            close(connection);
        }
    }

    public boolean containsPlayer(String uuid) throws SQLException {
        Connection connection = null;

        PreparedStatement ps = null;
        ResultSet set = null;

        try {
            connection = source.getConnection();

            ps = connection.prepareStatement("SELECT * FROM `players` WHERE `uuid`=?");
            ps.setString(1, uuid);

            set = ps.executeQuery();

            return set.next();
        } finally {
            if (set != null) set.close();
            if (ps != null) ps.close();

            close(connection);
        }
    }

    public int getTotalPlayers() throws SQLException {
        Connection connection = null;

        PreparedStatement ps = null;
        ResultSet set = null;

        try {
            connection = source.getConnection();

            ps = connection.prepareStatement("SELECT count(*) FROM `players`");
            set = ps.executeQuery();

            return set.getInt(1);
        } finally {
            if (set != null) set.close();
            if (ps != null) ps.close();

            close(connection);
        }
    }

    public void updateRank(String uuid, Rank rank) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = source.getConnection();

            ps = connection.prepareStatement("UPDATE `players` SET `rank`=? WHERE `uuid`=?");
            ps.setString(1, rank.getName());
            ps.setString(2, uuid);

            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();

            close(connection);
        }
    }

    public void updateRank(String uuid, String name, Rank rank) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = source.getConnection();

            ps = connection.prepareStatement("INSERT INTO players(`uuid`,`name`,`ip`, `rank`) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE `rank`=?");
            ps.setString(1, uuid);
            ps.setString(2, name);
            ps.setString(3, "Unknown");
            ps.setString(4, rank.getName());
            ps.setString(5, rank.getName());

            ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();

            close(connection);
        }
    }

    public String convertUUIDToName(String uuid) throws SQLException {
        Connection connection = null;

        PreparedStatement ps = null;
        ResultSet set = null;

        try {
            connection = source.getConnection();

            ps = connection.prepareStatement("SELECT * FROM `players` WHERE `uuid`=? ORDER BY `last_seen` DESC");
            ps.setString(1, uuid);

            set = ps.executeQuery();

            if (set.next()) {
                return set.getString("name");
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
