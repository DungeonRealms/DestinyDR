package net.dungeonrealms.packet.player;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketPlayerConnect implements Packet {

    private String player;
    private String server;

    public PacketPlayerConnect(String player, String server) {
        this.player = player;
        this.server = server;
    }

    public String getPlayer() {
        return player;
    }

    public String getServer() {
        return server;
    }

}
