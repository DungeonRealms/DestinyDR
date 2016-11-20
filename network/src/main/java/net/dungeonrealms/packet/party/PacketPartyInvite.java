package net.dungeonrealms.packet.party;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketPartyInvite implements Packet {

    private String sender;
    private String receiver;

    public PacketPartyInvite(String sender, String receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

}
