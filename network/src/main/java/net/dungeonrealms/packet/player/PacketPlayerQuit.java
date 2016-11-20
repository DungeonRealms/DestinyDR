package net.dungeonrealms.packet.player;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketPlayerQuit implements Packet {

    private String uuid;

    public PacketPlayerQuit(String uuid) {
        this.uuid = uuid;
    }

    public String getUUID() {
        return uuid;
    }

}
