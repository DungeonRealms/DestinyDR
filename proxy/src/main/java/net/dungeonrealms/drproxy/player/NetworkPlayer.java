package net.dungeonrealms.drproxy.player;

import net.dungeonrealms.drproxy.player.rank.Rank;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Created by Evoltr on 11/30/2016.
 */
public class NetworkPlayer {

    private String uuid;
    private String name;

    private Rank rank;

    public NetworkPlayer(String uuid, String name, Rank rank) {
        this.uuid = uuid;
        this.name = name;
        this.rank = rank;
    }

    public String getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Rank getRank() {
        return rank;
    }

    public ProxiedPlayer getProxiedPlayer() {
        return ProxyServer.getInstance().getPlayer(name);
    }

}
