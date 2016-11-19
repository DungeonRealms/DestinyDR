package net.dungeonrealms.control.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.player.DRPlayer;
import net.dungeonrealms.control.server.Server;
import net.dungeonrealms.control.server.types.GameServer;
import net.dungeonrealms.control.server.types.ProxyServer;
import net.dungeonrealms.control.utils.UtilLogger;
import net.dungeonrealms.packet.connect.PacketConnect;
import net.dungeonrealms.packet.network.PacketMOTD;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Evoltr on 11/19/2016.
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private Map<Channel, Server> connections = new HashMap<>();
    private Map<String, String> messages = new HashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object object) {
        Server server = connections.get(channelHandlerContext.channel());

        if (server == null) {

            // Handle incoming connection.
            if (object instanceof PacketConnect) {
                PacketConnect packetConnect = (PacketConnect) object;

                // Get the server the connection is coming from.
                if (packetConnect.getType().equals("PROXY")) {
                    server = DRControl.getInstance().getServerManager().getProxyServer(packetConnect.getServer());
                    UtilLogger.debug("Received proxy packet from " + packetConnect.getServer());
                } else if (packetConnect.getType().equals("SERVER")) {
                    server = DRControl.getInstance().getServerManager().getGameServer(packetConnect.getServer());
                    UtilLogger.debug("Received server packet from " + packetConnect.getServer());
                }

                // Get the IP of the incoming connection.
                String ip = ((InetSocketAddress) channelHandlerContext.channel().remoteAddress()).getHostName();

                // Check the ip adress matches the server for security reasons.
                if (server != null && server.getHost().equals(ip) || ip.equals("localhost")) {
                    onConnected(channelHandlerContext.channel(), server);
                }
            }
            return;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) {
        Server server = connections.get(channelHandlerContext.channel());

        if (server != null) {
            UtilLogger.info(server.getName() + " is now OFFLINE!");
        }

        // Set the server offline.
        server.setChannel(null);

        for (DRPlayer player : server.getPlayers()) {
            server.removePlayer(player.getUuid());
        }

        // Send packet to lobbies and proxies and tell them the server is offline.
        if (server instanceof GameServer) {
            GameServer gameServer = (GameServer) server;

            gameServer.setGame("Unknown");
            gameServer.setMap("Unknown");
            gameServer.setState("Offline");

            gameServer.sendInfoToServers();
        }

        // Remove the connection from the hashmap
        connections.remove(channelHandlerContext.channel());
    }

    public void onConnected(Channel channel, Server server) {
        UtilLogger.info(server.getName() + " is now ONLINE.");

        // Set the channel in the server class.
        server.setChannel(channel);

        // Register the connection in the hashmap.
        connections.put(channel, server);

        // Send packets to the proxy to sync it's info with the network.
        if (server instanceof ProxyServer) {
            server.sendPacket(new PacketMOTD("Test"));
        }

        // Send packets to the server to sync it's info with the network.
        if (server instanceof GameServer) {

            // Send packet to lobbies and proxies to tell them the server is online.
            ((GameServer) server).sendInfoToServers();
        }

        // If the server is a proxy or lobby we need to send the latest server info.
        if (server instanceof ProxyServer || ((GameServer) server).getType() == GameServer.ServerType.LOBBY) {

            // Send info for all game servers.
            for (GameServer gameServer : DRControl.getInstance().getServerManager().getGameServers()) {
                server.sendPacket(gameServer.createInfoPacket());
            }

        }
    }
}
