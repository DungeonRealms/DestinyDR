package net.dungeonrealms.packet.party;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketPartyJoin implements Packet {
    private String sender;
    private String target;

    public PacketPartyJoin(String sender, String target) {
        this.sender = sender;
        this.target = target;
    }

    public String getSender() {
        return sender;
    }

    public String getTarget() {
        return target;
    }

}