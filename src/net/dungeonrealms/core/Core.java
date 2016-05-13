package net.dungeonrealms.core;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Created by Nick on 12/12/2015.
 */
public final class Core implements GenericMechanic {

    private static Core instance = null;

    public static Core getInstance() {
        if (instance == null) {
            instance = new Core();
        }
        return instance;
    }

    public Connection connection;

    public synchronized void connectToMysql() {
        Utils.log.warning("DR | Connecting to MySQL ... This might take a moment ...");
        try {
            connection = DriverManager.getConnection("jdbc:mysql://192.99.200.110:3306/drnew?user=root&password=19584!cK");
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.shutdown();
        }

        verifyDatabaseIntegrity();

    }

    void verifyDatabaseIntegrity() {
        Utils.log.info("DR | Verifying Database Integrity... CHECKING ...");
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet guildTable = dbm.getTables(null, null, "guilds", null);
            if (guildTable.next()) {
                Utils.log.info("DR | Successfully located the table `guilds` ! GOOD ;-)");
            } else {
                Utils.log.warning("DR | ERROR Cannot find the table `guilds` in the database! Creating it now...");
                // Table does not exist
                try (
                        Statement statement = connection.createStatement()
                ) {
                    statement.execute("CREATE TABLE guilds (" +
                            "guildName VARCHAR(17) NOT NULL," +
                            "clanTag VARCHAR(4) NOT NULL," +
                            "data MEDIUMTEXT);");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Create Players Table
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet guildTable = dbm.getTables(null, null, "players", null);
            if (guildTable.next()) {
                Utils.log.info("DR | Successfully located the table `players` ! GOOD ;-)");
            } else {
                Utils.log.warning("DR | ERROR Cannot find the table `players` in the database! Creating it now...");
                // Table does not exist
                try (
                        Statement statement = connection.createStatement()
                ) {
                    statement.execute("CREATE TABLE players (" +
                            "name VARCHAR(17) NOT NULL," +
                            "uuid VARCHAR(36) NOT NULL, " +
                            "guild VARCHAR(17)); ");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Create Bounties Table
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet guildTable = dbm.getTables(null, null, "bounties", null);
            if (guildTable.next()) {
                Utils.log.info("DR | Successfully located the table `bounties` ! GOOD ;-)");
            } else {
                Utils.log.warning("DR | ERROR Cannot find the table `bounties` in the database! Creating it now...");
                // Table does not exist
                try (
                        Statement statement = connection.createStatement()
                ) {
                    statement.execute("CREATE TABLE bounties (" +
                            "placer VARCHAR(36) NOT NULL," +
                            "reward INTEGER NOT NULL," +
                            "victim VARCHAR(36) NOT NULL," +
                            "created BIGINT NOT NULL);");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param uuid players uuid.
     * @param name players name.
     */
    public void verifyPlayerIntegrity(UUID uuid, String name) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (
                    PreparedStatement statement = this.connection.prepareStatement("SELECT uuid FROM `players` WHERE uuid='" + uuid.toString() + "';");
                    ResultSet resultSet = statement.executeQuery()
            ) {

                if (!resultSet.next()) {
                    PreparedStatement addPlayer = Core.getInstance().connection.prepareStatement(
                            "INSERT INTO players VALUES(" + "'" + name + "'" + ", " + "'" + uuid.toString() + "'" + ", " + null + ");");
                    addPlayer.executeUpdate();
                    Utils.log.info("DR | Added new player: " + name + " " + uuid.toString());
                } else {
                    PreparedStatement updatePlayer = Core.getInstance().connection.prepareStatement("UPDATE `players` SET name='" + name + "' WHERE uuid='" + uuid.toString() + "';");
                    updatePlayer.executeUpdate();
                    Utils.log.info("DR | Updated player: " + name + " " + uuid.toString());
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * @param action
     */
    public void fetchOfflinePlayer(String playerName, Consumer<HashMap<String, Object>> action) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (
                    PreparedStatement statement = Core.getInstance().connection.prepareStatement("SELECT * FROM `players` WHERE name='" + playerName + "';");
                    ResultSet resultSet = statement.executeQuery()
            ) {
                HashMap<String, Object> temp = new HashMap<>();
                while (resultSet.next()) {
                    temp.put("name", resultSet.getObject("name"));
                    temp.put("uuid", resultSet.getObject("uuid"));
                    temp.put("guild", resultSet.getObject("guild"));
                }

                action.accept(temp);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Check if a player has played before.
     *
     * @param name
     * @return
     */
    public boolean hasPlayedBefore(String name) {
        Future<?> hasPlayed = Executors.newSingleThreadExecutor().submit(() -> {
            boolean played = false;
            try (
                    PreparedStatement statement = this.connection.prepareStatement("SELECT name FROM `players` WHERE name='" + name + "';");
                    ResultSet resultSet = statement.executeQuery()
            ) {
                played = resultSet.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return played;
        });
        try {
            return (Boolean) hasPlayed.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param name target player name.
     */
    public UUID getUUIDFromName(String name) {
        Future<?> uuid = Executors.newSingleThreadExecutor().submit(() -> {
            UUID rname = null;
            try (
                    PreparedStatement statement = this.connection.prepareStatement("SELECT uuid FROM `players` WHERE name='" + name + "';");
                    ResultSet resultSet = statement.executeQuery()
            ) {
                if (resultSet.next()) {
                    rname = UUID.fromString(resultSet.getString("uuid"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return rname;
        });
        try {
            return (UUID) uuid.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param uuid The uuid
     */
    public String getNameFromUUID(UUID uuid) {
        Future<?> name = Executors.newSingleThreadExecutor().submit(() -> {
            String rname = "";
            try (
                    PreparedStatement statement = this.connection.prepareStatement("SELECT name FROM `players` WHERE uuid='" + uuid.toString() + "';");
                    ResultSet resultSet = statement.executeQuery()
            ) {
                if (resultSet.next()) {
                    rname = resultSet.getString("name");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return rname;
        });
        try {
            return (String) name.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "NULL";
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.ARCHBISHOPS;
    }

    @Override
    public void startInitialization() {
        connectToMysql();
    }

    @Override
    public void stopInvocation() {

    }
}
