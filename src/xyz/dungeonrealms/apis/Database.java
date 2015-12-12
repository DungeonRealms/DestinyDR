package xyz.dungeonrealms.apis;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import xyz.dungeonrealms.DungeonRealms;
import xyz.dungeonrealms.mechanics.DRMechanic;
import xyz.dungeonrealms.utilities.UUIDFetcher;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by Nick on 12/10/2015.
 */
@SuppressWarnings("unchecked")
public class Database implements DRMechanic {

    private static Database instance = null;

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public Connection connection;

    public String connection_url = "mysql://$user$:$pass$@$host$/$database$?autoReconnect=true&useUnicode=yes";

    public HashMap<UUID, JSONObject> players = new HashMap<>();

    @Override
    public void onStart() {
        connectToMysql();
    }

    @Override
    public void onDisable() {
    }

    public synchronized void connectToMysql() {
        DungeonRealms.log.warning("DR | Connecting to MySQL ... This might take a moment ...");
        connection_url = connection_url.replace("$user$", "nickdor1_drAdmin").replace("$pass$", "bignig123").replace("$host$", "shmozo.com").replace("$database$", "nickdor1_dungeonrealms");

        try {
            connection = DriverManager.getConnection(connection_url);
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.shutdown();
        }

        verifyDatabaseIntegrity();

    }

    void verifyDatabaseIntegrity() {
        DungeonRealms.log.info("DR | Verifying Database Integrity... CHECKING ...");
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "players", null);
            if (tables.next()) {
                DungeonRealms.log.info("DR | Successfully located the table `players` ! GOOD ;-)");
            } else {
                DungeonRealms.log.warning("DR | ERROR Cannot find the table `players` in the database! Creating it now...");
                // Table does not exist
                try (
                        Statement statement = connection.createStatement();
                ) {
                    statement.execute("CREATE TABLE players (" +
                            "uuid VARCHAR(35) NOT NULL," +
                            "name VARCHAR(17) NOT NULL," +
                            "data MEDIUMBLOB);");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handlePlayerLogin(UUID uuid) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery("SELECT data FROM players WHERE uuid=" + uuid.toString() + ";");) {
                if (!resultSet.next()) {
                    createNewPlayer(uuid);
                } else {
                    //Removed the while(resultSet.next())
                    JSONObject jsonObject = (JSONObject) new JSONParser().parse(resultSet.getString("data"));
                    players.put(uuid, jsonObject);
                }
            } catch (SQLException | ParseException e) {
                e.printStackTrace();
            }
        });
    }

    public void createNewPlayer(UUID uuid) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (Statement statement = connection.createStatement();) {
                String name = UUIDFetcher.getName(uuid);
                statement.executeUpdate("INSERT INTO players VALUES (" + uuid.toString() + ", " + name + ", " + getNewPlayerJson(uuid, name).toJSONString() + ");");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                handlePlayerLogin(uuid);
            }
        });
    }

    JSONObject getNewPlayerJson(UUID uuid, String name) {
        JSONObject temp = new JSONObject();

        temp.put("uuid", uuid.toString());
        temp.put("name", name);
        temp.put("level", 1);
        temp.put("experience", 0);

        temp.put("guild", "");
        temp.put("achievements", new ArrayList<String>());

        return temp;
    }

    public void fetchOfflineGuild(String guildName, Consumer<JSONObject> object) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try (
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT data FROM guilds WHERE name=" + guildName + ";");
            ) {
                if (!resultSet.next()) {
                    object.accept(null);
                } else {
                    object.accept((JSONObject) new JSONParser().parse(resultSet.getString("data")));
                }

            } catch (SQLException | ParseException e) {
                e.printStackTrace();
            }
        });
    }

}
