package net.dungeonrealms.control.server.types;

import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.player.rank.Rank;
import net.dungeonrealms.control.server.Server;
import net.dungeonrealms.packet.server.PacketServerInfo;

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

    public void sendInfoToServers() {
        PacketServerInfo packetServerInfo = createInfoPacket();

        // Send the packet to all proxies.
        for (ProxyServer proxy : DRControl.getInstance().getServerManager().getProxyServers()) {
            proxy.sendPacket(packetServerInfo);
        }

        // Send the packet to all lobbies.
        for (GameServer lobby : DRControl.getInstance().getServerManager().getGameServers(ServerType.LOBBY)) {
            lobby.sendPacket(packetServerInfo);
        }
    }

    public PacketServerInfo createInfoPacket() {
        PacketServerInfo packet = new PacketServerInfo(getName(), getType().toString(), getDisplayName(), getHost(), getPort());

        packet.setOnline(isOnline());
        packet.setOnlinePlayers(getPlayers().size());
        packet.setMaxPlayers(getMaxPlayers());
        packet.setGame(getGame());
        packet.setMap(getMap());
        packet.setState(getState());

        return packet;
    }


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
