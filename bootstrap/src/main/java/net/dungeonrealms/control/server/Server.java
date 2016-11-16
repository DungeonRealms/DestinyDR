package net.dungeonrealms.control.server;

import io.netty.channel.Channel;
import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.player.DRPlayer;
import net.dungeonrealms.network.packet.Packet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evoltr on 11/15/2016.
 */
public class Server {

    private String name;
    private String host;
    private int port;

    private Channel channel = null;

    private List<String> players = new ArrayList<>();

    private long lastStartup = 0L;
    private long lastShutdown = 0L;
    private long lastOnline = System.currentTimeMillis();

    public Server(String name, String host, int port) {
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;

        if (channel != null) {
            lastStartup = System.currentTimeMillis();
        } else {
            lastShutdown = System.currentTimeMillis();
        }
    }

    public List<DRPlayer> getPlayers() {
        List<String> clonedList = new ArrayList<>();
        List<DRPlayer> playerList = new ArrayList<>();

        clonedList.addAll(players);

        // Convert uuids to player objects.
        for (String uuid : clonedList) {
            DRPlayer player = DRControl.getInstance().getPlayerManager().getPlayerByUUID(uuid);

            if (player != null) {
                playerList.add(player);
            }
        }
        return playerList;
    }

    public void addPlayer(String uuid) {
        players.add(uuid);
    }

    public void removePlayer(String uuid) {
        players.remove(uuid);
    }

    public boolean containsPlayer(DRPlayer player) {
        return players.contains(player.getUuid());
    }

    public void sendPacket(Packet packet) {
        if (getChannel() != null) {
            getChannel().writeAndFlush(packet);
        }
    }

    public boolean isOnline() {
        return getChannel() != null;
    }

    public long getTimeOnline() {
        return isOnline() ? System.currentTimeMillis() - lastStartup : -1L;
    }

    public long getLastOnline() {
        return lastOnline;
    }
}
