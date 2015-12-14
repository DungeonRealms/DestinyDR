package net.dungeonrealms.core;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import org.bukkit.Bukkit;

import java.sql.*;

/**
 * Created by Nick on 12/12/2015.
 */
public final class Core implements GenericMechanic{

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
            connection = DriverManager.getConnection("jdbc:mysql://192.99.43.236:3306/dungeonrealms?user=dungeonrealms&password=8aQbrSTsVDG7UWQh");
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
            ResultSet tables = dbm.getTables(null, null, "guilds", null);
            if (tables.next()) {
                Utils.log.info("DR | Successfully located the table `guilds` ! GOOD ;-)");
            } else {
                Utils.log.warning("DR | ERROR Cannot find the table `guilds` in the database! Creating it now...");
                // Table does not exist
                try (
                        Statement statement = connection.createStatement();
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
