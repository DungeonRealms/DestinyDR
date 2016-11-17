package net.dungeonrealms.control.server.types;

import net.dungeonrealms.control.player.rank.Rank;
import net.dungeonrealms.control.server.Server;

/**
 * Created by Evoltr on 11/15/2016.
 */
public class GameServer extends Server {

    private ServerType type;
    private String displayName;
    private Rank rank;

    private String game = "Unknown";
    private String map = "Unknown";
    private String state = "Booting up...";

    private int maxPlayers;

    public GameServer(String name, String displayName, String host, int port, ServerType type, int maxPlayers, Rank rank) {
        super(name, host, port);

        this.type = type;
        this.maxPlayers = maxPlayers;
        this.rank = rank;
        this.displayName = displayName;
    }

    public ServerType getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Rank getRank() {
        return rank;
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

    public int getMaxPlayers() {
        return maxPlayers;
    }

    //TODO: Make this


    public enum ServerType {
        LOBBY,
        DRINSTANCE;

        public static ServerType getByName(String name) {
            for (ServerType type : values()) {
                if (type.toString().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return null;
        }
    }
}
