package net.dungeonrealms.common.network.bungeecord;


import lombok.Data;
import net.dungeonrealms.common.network.ping.PingResponse;

import java.util.List;

@Data
public class BungeeServerInfo {

    // THIS WILL NEVER CHANGE //
    private final String serverName;

    private volatile boolean isOnline;
    private volatile int onlinePlayers;
    private volatile int maxPlayers;
    // The two lines of a motd
    private volatile String motd1; // Should never be null
    private volatile String motd2; // Should never be null
    private volatile long lastRequest;

    private List<PingResponse.PlayerInfo> sample;

    public BungeeServerInfo(String serverName) {
        this.serverName = serverName;
        isOnline = false;
        this.motd1 = "";
        this.motd2 = "";
        updateLastRequest();
    }

    public void setMotd(String motd) {
        if (motd == null) {
            this.motd1 = "";
            this.motd2 = "";
            return;
        }

        if (motd.contains("\n")) {
            String[] split = motd.split("\n");
            this.motd1 = split[0];
            this.motd2 = split[1];
        } else {
            this.motd1 = motd;
            this.motd2 = "";
        }
    }

    public void updateLastRequest() {
        this.lastRequest = System.currentTimeMillis();
    }

}
