package net.dungeonrealms.packet.player;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketPlayerJoin implements Packet {

    private String uuid;
    private String name;
    private String rank;

    public PacketPlayerJoin(String uuid, String name, String rank) {
        this.uuid = uuid;
        this.name = name;
        this.rank = rank;
    }

    public String getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getRank() {
        return rank;
    }

}
