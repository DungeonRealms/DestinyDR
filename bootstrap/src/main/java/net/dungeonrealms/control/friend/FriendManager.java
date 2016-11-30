package net.dungeonrealms.control.friend;

import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.player.DRPlayer;
import net.dungeonrealms.control.server.types.GameServer;
import net.dungeonrealms.control.utils.UtilLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Evoltr on 11/20/2016.
 */
public class FriendManager {

    private DRControl control;

    private Map<String, List<String>> friendLists = new HashMap<>();
    private Map<String, List<String>> friendRequests = new HashMap<>();

    public FriendManager(DRControl control) {
        this.control = control;
    }

    public List<DRPlayer> getFriends(DRPlayer player) {
        List<DRPlayer> players = new ArrayList<>();

        // Load the friend list and cache it.
        if (!friendLists.containsKey(player.getUuid())) {
            loadFriends(player);
        }

        // Convert all the uuids to player objects.
        players.addAll(friendLists.get(player.getUuid()).stream().map(uuid -> DRControl.getInstance().getPlayerManager().getPlayerByUUID(uuid)).collect(Collectors.toList()));

        return players;
    }

    public List<DRPlayer> getRequests(DRPlayer player) {
        List<DRPlayer> players = new ArrayList<>();

        // Load the friend list and cache it
        if (!friendLists.containsKey(player.getUuid())) {
            loadFriends(player);
        }

        players.addAll(friendRequests.get(player.getUuid()).stream().map(uuid -> DRControl.getInstance().getPlayerManager().getPlayerByUUID(uuid)).collect(Collectors.toList()));

        return players;
    }

    public boolean isFriend(DRPlayer p1, DRPlayer p2) {
        return getFriends(p1).contains(p2);
    }

    public String getStatus(DRPlayer player) {
        GameServer gameServer = player.getServer();

        if (gameServer == null) {
            return "&e is in an unknown dimension";
        }

        if (gameServer.getType() == GameServer.ServerType.LOBBY) {
            return "&e is in the main lobby!";
        }

        if (gameServer.getType() == GameServer.ServerType.DRINSTANCE) {
            return "&e is on instance " + gameServer.getName().toUpperCase();
        }

        return "&e is lost in an unknown dimension.";
    }

    public int getFriendLimit(DRPlayer player) {
        switch (player.getRank()) {
            case DEFAULT:
                return 15;
            case SUB:
                return 25;
            case SUB_2:
                return 35;
            case SUB_3:
                return 45;
        }
        return -1;
    }

    public void loadFriends(DRPlayer player) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = control.getDatabase().getConnection();

            preparedStatement = connection.prepareStatement("SELECT * FROM `friends` WHERE `sender`=? OR `receiver`=?");

            preparedStatement.setString(1, player.getUuid());
            preparedStatement.setString(2, player.getUuid());

            resultSet = preparedStatement.executeQuery();

            List<String> friends = new ArrayList<>();
            List<String> requests = new ArrayList<>();

            while (resultSet.next()) {
                String senderUUID = resultSet.getString("sender");
                String receiverUUID = resultSet.getString("receiver");

                String otherPlayer = player.getUuid().equals(senderUUID) ? receiverUUID : senderUUID;

                // The entry is an established friendship.
                if (resultSet.getBoolean("accepted")) {
                    friends.add(otherPlayer);
                } else if (player.getUuid().equals(receiverUUID)) {
                    requests.add(senderUUID);
                }
            }

            friendLists.put(player.getUuid(), friends);
            friendRequests.put(player.getUuid(), requests);
        } catch (Exception e) {
            UtilLogger.warn("Database error: " + e.getMessage());
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                connection.close();
            } catch (Exception e) {
                // Ignore.
            }
        }

    }

    public void createFriendship(String sender, String receiver, boolean accepted) {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = control.getDatabase().getConnection();

            ps = connection.prepareStatement("INSERT INTO `friends`(`sender`, `receiver`, `accepted`) VALUES(?,?,?)");

            ps.setString(1, sender);
            ps.setString(2, receiver);
            ps.setBoolean(3, accepted);

            ps.execute();
        } catch (Exception e) {
            UtilLogger.warn("Database error: " + e.getMessage());
        } finally {
            if (ps != null) try {
                ps.close();
            } catch (Exception e) {
                // Ignore.
            }
        }
    }

    public void updateFriendship(String sender, String receiver, boolean accepted) {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = control.getDatabase().getConnection();

            ps = connection.prepareStatement("UPDATE `friends` SET `accepted`=? WHERE (sender=? AND receiver=?) OR (sender=? AND receiver=?)");

            ps.setBoolean(1, accepted);
            ps.setString(2, sender);
            ps.setString(3, receiver);
            ps.setString(4, receiver);
            ps.setString(5, sender);

            ps.execute();
        } catch (Exception e) {
            UtilLogger.warn("Database error: " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();

                connection.close();
            } catch (Exception e) {
                //Ignore.
            }
        }
    }

    public void removeFriendship(String sender, String receiver) {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = control.getDatabase().getConnection();

            ps = connection.prepareStatement("DELETE FROM `friends` WHERE (sender=? AND receiver=?) OR (sender=? AND receiver=?)");

            ps.setString(1, sender);
            ps.setString(2, receiver);
            ps.setString(3, receiver);
            ps.setString(4, sender);

            ps.execute();
        } catch (Exception e) {
            UtilLogger.warn("Database error: " + e.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();

                connection.close();
            } catch (Exception e) {
                //Ignore.
            }
        }
    }

}
