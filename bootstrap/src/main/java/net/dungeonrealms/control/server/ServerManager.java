package net.dungeonrealms.control.server;

import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.server.types.GameServer;
import net.dungeonrealms.control.server.types.ProxyServer;

import java.util.ArrayList;
import java.util.List;

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
}
