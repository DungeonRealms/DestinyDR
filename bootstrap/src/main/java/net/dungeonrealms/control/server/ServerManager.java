package net.dungeonrealms.control.server;

import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.player.DRPlayer;
import net.dungeonrealms.control.player.rank.Rank;
import net.dungeonrealms.control.server.types.GameServer;
import net.dungeonrealms.control.server.types.ProxyServer;
import net.dungeonrealms.control.utils.UtilLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Evoltr on 11/15/2016.
 */
public class ServerManager {

    private DRControl control;

    private List<ProxyServer> proxyServers = new ArrayList<>();
    private List<GameServer> gameServers = new ArrayList<>();

    public ServerManager(DRControl control) throws SQLException {
        this.control = control;
        loadProxies();
        loadServers();
    }

    public List<ProxyServer> getProxyServers() {
        return proxyServers;
    }

    public List<GameServer> getGameServers() {
        return gameServers;
    }

    public GameServer getGameServer(String name) {
        for (GameServer server : getGameServers()) {
            if (server.getName().equalsIgnoreCase(name)) {
                return server;
            }
        }
        return null;
    }

    public ProxyServer getProxyServer(String name) {
        for (ProxyServer server : getProxyServers()) {
            if (server.getName().equalsIgnoreCase(name)) {
                return server;
            }
        }
        return null;
    }

    public List<GameServer> getOnlineServers() {
        List<GameServer> servers = new ArrayList<>();

        // Loop through all interactable instances and check if they're online.
        servers.addAll(getGameServers().stream().filter(server -> server.isOnline()).collect(Collectors.toList()));

        return servers;
    }

    public List<ProxyServer> getOnlineProxies() {
        List<ProxyServer> servers = new ArrayList<>();

        // Loop through all bungee instances and check if they're online.
        servers.addAll(getProxyServers().stream().filter(server -> server.isOnline()).collect(Collectors.toList()));

        return servers;
    }

    public List<GameServer> getGameServers(GameServer.ServerType type) {
        List<GameServer> servers = new ArrayList<>();

        // Loop through all servers and compare the type.
        servers.addAll(getGameServers().stream().filter(server -> server.getType() == type).collect(Collectors.toList()));

        return servers;
    }

    public List<GameServer> getOnlineServers(GameServer.ServerType type) {
        List<GameServer> servers = new ArrayList<>();

        // Loop through all servers and check if they're online.
        servers.addAll(getGameServers(type).stream().filter(server -> server.isOnline()).collect(Collectors.toList()));

        return servers;
    }

    public GameServer getBestServer(GameServer.ServerType type, DRPlayer player) {
        GameServer best = null;

        // The logic for lobbies is different so handled in it's own method.
        if (type == GameServer.ServerType.LOBBY) {
            return getBestLobby(player);
        }

        // Calculate the best server.
        for (GameServer server : getOnlineServers(type)) {
            if (server.isFull() || server.getState().contains("In Game") || player.getRank().getID() < server.getRank().getID()) {
                continue;
            }

            if (best == null || server.getPlayers().size() > best.getPlayers().size()) {
                best = server;
            }
        }

        return best;
    }

    private GameServer getBestLobby(DRPlayer player) {
        List<GameServer> lobbies = getAvailableLobbies();

        // Put donators in the premium lobby.
        if (player.getRank().getID() >= Rank.SUB.getID() && getGameServer("donorlobby").isOnline() && DRControl.getInstance().getPartyManager().getParty(player) == null) {
            return getGameServer("donorlobby");
        }

        GameServer bestLobby = null;

        for (GameServer lobby : lobbies) {
            if (bestLobby == null || lobby.getPlayers().size() < bestLobby.getPlayers().size()) {
                bestLobby = lobby;
            }
        }

        return bestLobby;
    }

    public List<GameServer> getAvailableLobbies() {
        List<GameServer> lobbies = new ArrayList<>();
        int lobbiesAvailable = ((getOnlinePlayers(GameServer.ServerType.LOBBY) + 1) / 60) + 1;

        // Calculate the lobbies to show.
        for (int x = 0; x < lobbiesAvailable; x++) {
            GameServer lobby = getGameServer("lobby" + (x + 1));

            if (lobby == null) {
                continue;
            }

            if (lobby.isOnline()) {
                lobbies.add(lobby);
            } else {
                lobbiesAvailable++;
            }
        }

        return lobbies;
    }

    public void autoRestartServers() {

        // Restart any lobby that has been online for more than 6 hours (only one at a time).
        for (GameServer server : getGameServers(GameServer.ServerType.LOBBY)) {
            if (server.getTimeOnline() >= 21600000L) {
                server.restart();
                break;
            }
        }

    }

    public void loadProxies() throws SQLException {
        Connection connection = null;

        PreparedStatement ps = null;
        ResultSet set = null;

        try {
            connection = control.getDatabase().getConnection();

            ps = connection.prepareStatement("SELECT * FROM `proxies`");
            set = ps.executeQuery();

            while (set.next()) {
                String name = set.getString("name");
                String host = set.getString("address");
                int port = set.getInt("port");

                try {
                    proxyServers.add(new ProxyServer(name, host, port));
                    UtilLogger.warn("Added server to arraylist: " + name + ", " + host + ":" + port);
                } catch (Exception e) {
                    UtilLogger.warn("Failed to add server to arraylist: " + name + ", " + host + ":" + port);
                }
            }

        } finally {
            if (set != null) set.close();
            if (ps != null) ps.close();

            try {
                connection.close();
            } catch (Exception e) {
                //Ignore.
            }
        }
    }

    public void loadServers() throws SQLException {
        Connection connection = null;

        PreparedStatement ps = null;
        ResultSet set = null;

        try {
            connection = control.getDatabase().getConnection();

            ps = connection.prepareStatement("SELECT * FROM `servers`");
            set = ps.executeQuery();

            while (set.next()) {
                String name = set.getString("name");
                String type = set.getString("type");
                String host = set.getString("address");
                String display = set.getString("display_name");

                int port = set.getInt("port");
                int max = set.getInt("max_players");

                Rank rank = Rank.getRank(set.getString("rank"));

                gameServers.add(new GameServer(name, display, host, port, GameServer.ServerType.valueOf(type), max, rank));
            }

        } finally {
            if (set != null) set.close();
            if (ps != null) ps.close();

            try {
                connection.close();
            } catch (Exception e) {
                //Ignore.
            }
        }
    }

    public int getOnlinePlayers() {
        int onlinePlayers = 0;

        // Add the online count of each proxy to the total value.
        for (ProxyServer server : getProxyServers()) {
            onlinePlayers += server.getPlayers().size();
        }

        return onlinePlayers;
    }

    public int getOnlinePlayers(GameServer.ServerType type) {
        int onlinePlayers = 0;

        // Add the online count of each server to the total value.
        for (GameServer server : getGameServers(type)) {
            onlinePlayers += server.getPlayers().size();
        }

        return onlinePlayers;
    }

    public int getMaxPlayers() {
        return getProxyServers().size() * 500;
    }
}
