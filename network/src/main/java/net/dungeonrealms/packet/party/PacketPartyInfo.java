package net.dungeonrealms.packet.party;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketPartyInfo implements Packet {

    private String player;

    private String partyOwner;

    public PacketPartyInfo(String player, String partyOwner) {
        this.player = player;
        this.partyOwner = partyOwner;
    }

    public String getPlayer() {
        return player;
    }

    public String getPartyOwner()
    {
        return partyOwner;
    }

}
