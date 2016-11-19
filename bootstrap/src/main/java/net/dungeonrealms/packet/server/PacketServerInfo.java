package net.dungeonrealms.packet.server;

import net.dungeonrealms.packet.Packet;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class PacketServerInfo implements Packet {

    private String name;
    private String type;
    private String display;

    private String host;
    private int port;

    private int onlinePlayers;
    private int maxPlayers;

    private String game;
    private String map;
    private String state;

    private boolean online = true;

    public PacketServerInfo(String name, String type, String display, String host, int port) {
        this.name = name;
        this.type = type;
        this.host = host;
        this.port = port;
        this.display = display;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDisplayName() {
        return display;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

}
