package net.dungeonrealms.packet.server;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketServerPing implements Packet {

    private String server;

    private String game;
    private String map;
    private String state;

    public PacketServerPing(String server, String game, String map, String state) {
        this.server = server;
        this.game = game;
        this.map = map;
        this.state = state;
    }

    public String getServer() {
        return server;
    }

    public String getGame() {
        return game;
    }

    public String getMap() {
        return map;
    }

    public String getState() {
        return state;
    }

}
