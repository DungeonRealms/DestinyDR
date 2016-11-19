package net.dungeonrealms.packet.network;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketStaffList implements Packet {

    private String player;

    public PacketStaffList(String player) {
        this.player = player;
    }

    public String getPlayer() {
        return player;
    }

}
