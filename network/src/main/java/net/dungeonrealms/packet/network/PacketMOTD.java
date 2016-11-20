package net.dungeonrealms.packet.network;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketMOTD implements Packet {

    private String motd;

    public PacketMOTD(String motd) {
        this.motd = motd;
    }

    public String getMOTD() {
        return motd;
    }

}
