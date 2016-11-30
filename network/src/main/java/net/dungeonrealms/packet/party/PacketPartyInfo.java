package net.dungeonrealms.packet.party;

import lombok.Getter;
import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketPartyInfo implements Packet {

    private String player;

    private String partyOwner;

    @Getter
    private boolean data;

    public PacketPartyInfo(String player, String partyOwner, boolean data) {
        this.player = player;
        this.partyOwner = partyOwner;
        this.data = data;
    }

    public String getPlayer() {
        return player;
    }

    public String getPartyOwner() {
        return partyOwner;
    }

}
