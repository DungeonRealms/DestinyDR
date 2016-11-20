package net.dungeonrealms.packet.network;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketPlayerCount implements Packet {

    private int onlinePlayers;
    private int maxPlayers;

    public PacketPlayerCount(int onlinePlayers, int maxPlayers) {
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

}
