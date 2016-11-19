package net.dungeonrealms.packet.player;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketMessage implements Packet {

    private String player;
    private String message;

    public PacketMessage(String player, String message) {
        this.player = player;
        this.message = message;
    }

    public String getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }

}
