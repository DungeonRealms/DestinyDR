package net.dungeonrealms.packet.connect;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketConnect implements Packet {

    private String type;
    private String server;

    public PacketConnect(String type, String server) {
        this.type = type;
        this.server = server;
    }

    public String getType() {
        return type;
    }

    public String getServer() {
        return server;
    }

}
