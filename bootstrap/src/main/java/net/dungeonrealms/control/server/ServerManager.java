package net.dungeonrealms.control.server;

import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.server.types.GameServer;
import net.dungeonrealms.control.server.types.ProxyServer;

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

    public ServerManager(DRControl control) {
        this.control = control;
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

    public List<GameServer> getOnlineGames() {
        List<GameServer> servers = new ArrayList<>();

        // Loop through all game instances and check if they're online.
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
}
