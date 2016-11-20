package net.dungeonrealms.control.player;

import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.party.Party;
import net.dungeonrealms.control.player.rank.Rank;
import net.dungeonrealms.control.server.types.GameServer;
import net.dungeonrealms.control.server.types.ProxyServer;
import net.dungeonrealms.packet.player.PacketMessage;
import net.dungeonrealms.packet.player.PacketPlayerConnect;

import java.util.List;

/**
 * Created by Evoltr on 11/15/2016.
 */
public class DRPlayer {

    private String uuid;
    private String name;
    private Rank rank;

    public DRPlayer(String uuid, String name, Rank rank) {
        this.uuid = uuid;
        this.name = name;
        this.rank = rank;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Rank getRank() {
        return rank;
    }

    public boolean isOnline() {
        return getProxy() != null;
    }

    public GameServer getServer() {
        List<GameServer> servers = DRControl.getInstance().getServerManager().getGameServers();

        // Check if server has the player
        for (GameServer server : servers) {
            if (server.containsPlayer(this)) {
                return server;
            }
        }
        return null;
    }

    public ProxyServer getProxy() {
        List<ProxyServer> servers = DRControl.getInstance().getServerManager().getProxyServers();

        // Check if server has the player
        for (ProxyServer server : servers) {
            if (server.containsPlayer(this)) {
                return server;
            }
        }
        return null;
    }

    public void sendMessage(String msg, boolean prefix) {
        ProxyServer proxyServer = getProxy();

        if (proxyServer != null) {
            proxyServer.sendPacket(new PacketMessage(getName(), (prefix ? "" : "") + msg));
        }
    }

    public boolean connect(GameServer server) {
        return connect(server, false);
    }

    public boolean connect(GameServer server, boolean ignoreParty) {

        if (server == null) {
            sendMessage("&cThat server doesn't exist.", true);
            return false;
        }

        if (getRank().getID() < server.getRank().getID()) {
            sendMessage("&cYou need the " + server.getRank().getName().toUpperCase() + " &crank to access this server.", true);
            return false;
        }

        if (!server.isOnline()) {
            sendMessage("&cThat server is currently offline.", true);
            return false;
        }

        if (getServer() == server) {
            sendMessage("&cYou're already connected to that server.", true);
            return false;
        }

        if (server.isFull() && getRank().getID() < Rank.SUB.getID()) {
            sendMessage("&cThat server is currently full.", true);
            sendMessage("&cPurchase &bPremium &cto access full servers: &b" + "http://dungeonrealms.net", true);
            return false;
        }

        Party party = DRControl.getInstance().getPartyManager().getParty(this);

        if (!ignoreParty && party != null) {

            if (!party.isOwner(this)) {
                sendMessage("&cOnly the party leader can join servers.", true);
                return false;
            }

            if (party.getPlayers().size() > server.getMaxPlayers() - server.getPlayers().size()) {
                sendMessage("&cThere is not enough slots available for your party to join that server.", true);
                return false;
            }

            boolean correctRank = true;

            // Check each party member can join the server.
            for (DRPlayer player : party.getPlayers()) {
                if (player.getRank().getID() < server.getRank().getID()) {
                    correctRank = false;
                }
            }

            if (!correctRank) {
                sendMessage("&cOne of more of your party members does not have the required rank to join this server.", true);
                return false;
            }

            //Connect each member to the server.
            party.getPlayers().stream().filter(member -> member.getServer() != server).forEach(member -> {
                member.connect(server, true);
            });

            return true;
        }

        if (getProxy() != null) {
            getProxy().sendPacket(new PacketPlayerConnect(getName(), server.getName()));
        }

        return true;
    }
}
