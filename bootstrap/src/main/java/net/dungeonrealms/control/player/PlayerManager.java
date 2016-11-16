package net.dungeonrealms.control.player;

import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.player.rank.Rank;
import net.dungeonrealms.control.utils.UtilLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Evoltr on 11/15/2016.
 */
public class PlayerManager {

    private DRControl control;

    private Map<String, DRPlayer> byUUID = new HashMap<>();
    private Map<String, DRPlayer> byName = new HashMap<>();

    public PlayerManager(DRControl control) {
        this.control = control;
    }

    public DRPlayer getPlayerByUUID(String uuid) {
        if (!byUUID.containsKey(uuid.toLowerCase())) {
            updatePlayerByUUID(uuid);
        }

        return byUUID.get(uuid.toLowerCase());
    }

    public DRPlayer getPlayerByName(String name) {
        if (!byName.containsKey(name.toLowerCase())) {
            updatePlayerByName(name);
        }

        return byName.get(name.toLowerCase());
    }

    public void updatePlayer(String uuid, String name, Rank rank) {
        DRPlayer player = new DRPlayer(uuid, name, rank);

        byUUID.put(uuid.toLowerCase(), player);
        byName.put(name.toLowerCase(), player);
    }


    public void updatePlayerByUUID(String uuid) {
        Connection connection = null;

        PreparedStatement preparedStatement = null;
        ResultSet set = null;

        try {
            connection = control.getDatabase().getConnection();

            preparedStatement = connection.prepareStatement("SELECT * FROM `players` WHERE `uuid`=?");
            preparedStatement.setString(1, uuid);

            set = preparedStatement.executeQuery();

            if (set.next()) {
                updatePlayer(set.getString("uuid"), set.getString("name"), Rank.getRank(set.getString("rank")));
            } else {
                byUUID.put(uuid.toLowerCase(), null);
            }

        } catch (Exception e) {
            UtilLogger.warn("Database error: " + e.getMessage());
        } finally {
            try {
                if (set != null) {
                    set.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }

                connection.close();
            } catch (Exception e) {
                //Ignore.
            }
        }
    }

    private void updatePlayerByName(String name) {
        Connection connection = null;

        PreparedStatement preparedStatement = null;
        ResultSet set = null;

        try {
            connection = control.getDatabase().getConnection();

            preparedStatement = connection.prepareStatement("SELECT * FROM `players` WHERE `name`=? ORDER BY `last_seen` DESC");
            preparedStatement.setString(1, name);

            set = preparedStatement.executeQuery();

            if (set.next()) {
                updatePlayer(set.getString("uuid"), set.getString("name"), Rank.getRank(set.getString("rank")));
            } else {
                byName.put(name.toLowerCase(), null);
            }

        } catch (Exception e) {
            UtilLogger.warn("Database error: " + e.getMessage());
        } finally {
            try {
                if (set != null) {
                    set.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }

                connection.close();
            } catch (Exception e) {
                //Ignore.
            }
        }
    }
}
