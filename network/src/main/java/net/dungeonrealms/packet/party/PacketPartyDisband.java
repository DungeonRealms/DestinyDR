package net.dungeonrealms.packet.party;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketPartyDisband implements Packet {

    private String player;

    public PacketPartyDisband(String player) {
        this.player = player;
    }

    public String getPlayer() {
        return player;
    }

}
