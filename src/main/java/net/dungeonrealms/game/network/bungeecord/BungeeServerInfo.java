package net.dungeonrealms.game.network.bungeecord;

public class BungeeServerInfo {

    private volatile boolean isOnline;
    private volatile int onlinePlayers;
    private volatile int maxPlayers;

    // The two lines of a motd
    private volatile String motd1; // Should never be null
    private volatile String motd2; // Should never be null

    private volatile long lastRequest;

    protected BungeeServerInfo() {
        isOnline = false;
        this.motd1 = "";
        this.motd2 = "";
        updateLastRequest();
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getMotd1() {
        return motd1;
    }

    public String getMotd2() {
        return motd2;
    }


    public long getLastRequest() {
        return lastRequest;
    }

    public void updateLastRequest() {
        this.lastRequest = System.currentTimeMillis();
    }

}
