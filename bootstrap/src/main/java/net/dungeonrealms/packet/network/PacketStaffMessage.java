package net.dungeonrealms.packet.network;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketStaffMessage implements Packet {

    private String message;

    public PacketStaffMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
