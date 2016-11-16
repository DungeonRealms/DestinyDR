package net.dungeonrealms.control.player;

import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.player.rank.Rank;
import net.dungeonrealms.control.server.types.GameServer;
import net.dungeonrealms.control.server.types.ProxyServer;

import java.util.List;

/**
 * Created by Evoltr on 11/15/2016.
 */
public class DRPlayer {

    private String uuid;
    private String name;
    private Rank rank;

    public DRPlayer(String uuid, String name, Rank rank) {
        this.uuid = uuid;
        this.name = name;
        this.rank = rank;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Rank getRank() {
        return rank;
    }

    public boolean isOnline() {
        return getProxy()!= null;
    }

    public GameServer getServer() {
        List<GameServer> servers = DRControl.getInstance().getServerManager().getGameServers();

        // Check if server has the player
        for (GameServer server : servers) {
            if (server.containsPlayer(this)) {
                return server;
            }
        }
        return null;
    }

    public ProxyServer getProxy() {
        List<ProxyServer> servers = DRControl.getInstance().getServerManager().getProxyServers();

        // Check if server has the player
        for (ProxyServer server : servers) {
            if (server.containsPlayer(this)) {
                return server;
            }
        }
        return null;
    }
}
