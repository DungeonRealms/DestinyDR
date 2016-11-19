package net.dungeonrealms.packet.party;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketPartyInfo implements Packet {

    private String player;

    public PacketPartyInfo(String player) {
        this.player = player;
    }

    public String getPlayer() {
        return player;
    }

}
