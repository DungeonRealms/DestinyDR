package net.dungeonrealms.control.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.dungeonrealms.control.DRControl;
import net.dungeonrealms.control.player.DRPlayer;
import net.dungeonrealms.control.player.rank.Rank;
import net.dungeonrealms.control.server.Server;
import net.dungeonrealms.control.server.types.GameServer;
import net.dungeonrealms.control.server.types.ProxyServer;
import net.dungeonrealms.control.utils.UtilLogger;
import net.dungeonrealms.packet.Packet;
import net.dungeonrealms.packet.connect.PacketConnect;
import net.dungeonrealms.packet.network.PacketMOTD;
import net.dungeonrealms.packet.network.PacketPrivateMessage;
import net.dungeonrealms.packet.network.PacketReply;
import net.dungeonrealms.packet.network.PacketStaffMessage;
import net.dungeonrealms.packet.party.*;
import net.dungeonrealms.packet.player.*;
import net.dungeonrealms.packet.player.in.PacketPlayerDataRequest;
import net.dungeonrealms.packet.player.out.PacketPlayerDataSend;
import net.dungeonrealms.packet.server.PacketServerPing;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

            // Handle incoming net.dungeonrealms.database.connection.
            if (object instanceof PacketConnect) {
                PacketConnect packetConnect = (PacketConnect) object;

                // Get the server the net.dungeonrealms.database.connection is coming from.
                if (packetConnect.getType().equals("PROXY")) {
                    server = DRControl.getInstance().getServerManager().getProxyServer(packetConnect.getServer());
                    UtilLogger.debug("Received proxy packet from " + packetConnect.getServer());
                } else if (packetConnect.getType().equals("SERVER")) {
                    server = DRControl.getInstance().getServerManager().getGameServer(packetConnect.getServer());
                    UtilLogger.debug("Received server packet from " + packetConnect.getServer());
                }

                // Get the IP of the incoming net.dungeonrealms.database.connection.
                String ip = ((InetSocketAddress) channelHandlerContext.channel().remoteAddress()).getHostName();

                // Check the ip adress matches the server for security reasons.
                if (server != null && server.getHost().equals(ip) || ip.equals("localhost")) {
                    onConnected(channelHandlerContext.channel(), server);
                }
                return;
            }

            // Convert the received message into a packet.
            Packet packet = (Packet) object;

            // Handle join packet.
            if (packet instanceof PacketPlayerJoin) {
                PacketPlayerJoin packetPlayerJoin = (PacketPlayerJoin) packet;

                // Update the player's data in the player manager.
                if (packetPlayerJoin.getRank() != null && Rank.getRank(packetPlayerJoin.getRank()) != null) {
                    DRControl.getInstance().getPlayerManager().updatePlayer(packetPlayerJoin.getUUID(), packetPlayerJoin.getName(), Rank.getRank(packetPlayerJoin.getRank()));
                }
                server.addPlayer(packetPlayerJoin.getUUID());

                // Send the new player count to the lobbies and proxies.
                if (server instanceof GameServer) {
                    ((GameServer) server).sendInfoToServers();
                }

                DRPlayer player = DRControl.getInstance().getPlayerManager().getPlayerByUUID(packetPlayerJoin.getUUID());

                // Send login message to all friends.
                if (server instanceof ProxyServer) {
                    for (DRPlayer friend : DRControl.getInstance().getFriendManager().getFriends(player)) {
                        player.sendMessage("&a+ " + friend.getName() + ".", true);
                    }

                    DRControl.getInstance().getChannel().eventLoop().schedule(() -> {
                        int friendRequests = DRControl.getInstance().getFriendManager().getRequests(player).size();
                        if (friendRequests > 0) {
                            player.sendMessage("You have " + friendRequests + " friend requests. Type '/f requests' to view them.", true);
                        }

                    }, 500, TimeUnit.MILLISECONDS);
                }
            }

            // Handle quit packet.
            if (packet instanceof PacketPlayerQuit) {
                PacketPlayerQuit packetPlayerQuit = (PacketPlayerQuit) packet;

                DRPlayer player = DRControl.getInstance().getPlayerManager().getPlayerByUUID(packetPlayerQuit.getUUID());

                // Send leave message to all friends.
                if (server instanceof ProxyServer) {
                    for (DRPlayer friend : DRControl.getInstance().getFriendManager().getFriends(player)) {
                        player.sendMessage("&c- " + friend.getName(), true);
                    }
                }

                server.removePlayer(packetPlayerQuit.getUUID());

                // Send the new player count to the lobbies and proxies.
                if (server instanceof GameServer) {
                    ((GameServer) server).sendInfoToServers();
                }
            }

            // Handle server ping packet.
            if (packet instanceof PacketServerPing) {
                PacketServerPing packetServerPing = (PacketServerPing) packet;

                if (server instanceof GameServer) {
                    GameServer gameServer = (GameServer) server;

                    gameServer.setGame(packetServerPing.getGame());
                    gameServer.setMap(packetServerPing.getMap());
                    gameServer.setState(packetServerPing.getState());

                    gameServer.sendInfoToServers();
                }
            }

            // Handle player connect packet.
            if (packet instanceof PacketPlayerConnect) {
                PacketPlayerConnect packetPlayerConnect = (PacketPlayerConnect) packet;

                DRPlayer player = DRControl.getInstance().getPlayerManager().getPlayerByName(packetPlayerConnect.getPlayer());
                String serverName = packetPlayerConnect.getServer();

                GameServer gameServer = DRControl.getInstance().getServerManager().getGameServer(serverName);

                if (GameServer.ServerType.getByName(serverName) != null) {
                    gameServer = DRControl.getInstance().getServerManager().getBestServer(GameServer.ServerType.getByName(serverName), player);
                }

                player.connect(gameServer);
            }

            // Handle staff message packet.
            if (packet instanceof PacketStaffMessage) {
                PacketStaffMessage packetStaffMessage = (PacketStaffMessage) packet;

                // Forward the packet to all proxies.
                for (ProxyServer proxy : DRControl.getInstance().getServerManager().getProxyServers()) {
                    proxy.sendPacket(packetStaffMessage);
                }
            }

            // Handle message packet.
            if (packet instanceof PacketMessage) {
                PacketMessage packetMessage = (PacketMessage) packet;

                DRPlayer player = DRControl.getInstance().getPlayerManager().getPlayerByName(packetMessage.getPlayer());

                // Send the message to the player.
                if (player != null) {
                    player.sendMessage(packetMessage.getMessage(), false);
                }
            }

            // Handle private message packet.
            if (packet instanceof PacketPrivateMessage) {
                PacketPrivateMessage packetPrivateMessage = (PacketPrivateMessage) packet;

                String sender = packetPrivateMessage.getSender();
                String receiver = packetPrivateMessage.getReceiver();
                String message = packetPrivateMessage.getMessage();

                //Handle the message.
                handleMessage(sender, receiver, message, false);
            }

            // Handle reply packet.
            if (packet instanceof PacketReply) {
                PacketReply packetReply = (PacketReply) packet;

                String sender = packetReply.getSender();
                String message = packetReply.getMessage();

                DRPlayer player = DRControl.getInstance().getPlayerManager().getPlayerByName(sender);

                // Get the last player they messaged from the hashmap.
                String receiver = messages.get(sender.toLowerCase());

                if (receiver == null) {
                    player.sendMessage("&cYou have not messaged anyone recently.", true);
                    return;
                }

                // Handle the message.
                handleMessage(sender, receiver, message, true);
            }


            // Handle party invite packet.
            if (packet instanceof PacketPartyInvite) {
                DRControl.getInstance().getPartyManager().handleInvite((PacketPartyInvite) packet);
            }

            // Handle party accept packet.
            if (packet instanceof PacketPartyAccept) {
                DRControl.getInstance().getPartyManager().handleAccept((PacketPartyAccept) packet);
            }

            // Handle party leave packet.
            if (packet instanceof PacketPartyLeave) {
                DRControl.getInstance().getPartyManager().handleLeave((PacketPartyLeave) packet);
            }

            // Handle party chat packet.
            if (packet instanceof PacketPartyChat) {
                DRControl.getInstance().getPartyManager().handleChat((PacketPartyChat) packet);
            }

            // Handle party warp packet.
            if (packet instanceof PacketPartyWarp) {
                DRControl.getInstance().getPartyManager().handleWarp((PacketPartyWarp) packet);
            }

            // Handle party disband packet.
            if (packet instanceof PacketPartyDisband) {
                DRControl.getInstance().getPartyManager().handleDisband((PacketPartyDisband) packet);
            }
        }
    }

    public void handleMessage(String sender, String receiver, String message, boolean isReply) {

        DRPlayer senderPlayer = DRControl.getInstance().getPlayerManager().getPlayerByName(sender);
        DRPlayer receiverPlayer = DRControl.getInstance().getPlayerManager().getPlayerByName(receiver);

        // Check if they are not messaging themselves.
        if (sender.equalsIgnoreCase(receiver)) {
            senderPlayer.sendMessage("&cYou cannot message yourself!", true);
            return;
        }

        // Check the receiving player exists.
        if (receiverPlayer == null) {
            senderPlayer.sendMessage("&cThat player is currently offline.", true);
            return;
        }

        // Check they're allowed to message this player.
        if (!isReply && !DRControl.getInstance().getFriendManager().isFriend(senderPlayer, receiverPlayer) && senderPlayer.getRank().getID() < Rank.PMOD.getID()) {
            senderPlayer.sendMessage("&cYou can only message players on your friend list.", true);
            senderPlayer.sendMessage("&cUse /f add <player> to add them.", true);
            return;
        }

        // Check the receiving player is online.
        if (!receiverPlayer.isOnline()) {
            senderPlayer.sendMessage("&cThat player is currently offline.", true);
            return;
        }

        // Send the messages to the players.
        if (receiverPlayer.getRank() == Rank.DEFAULT) {
            senderPlayer.sendMessage("&dTo " + receiverPlayer.getName() + "&7: " + message, false);
        } else {
            senderPlayer.sendMessage("&dTo " + receiverPlayer.getRank().getColor() + "[" + receiverPlayer.getRank().getName() + "] " + receiverPlayer.getName() + "&7: " + message, false);
        }

        if (senderPlayer.getRank() == Rank.DEFAULT) {
            receiverPlayer.sendMessage("&dFrom " + senderPlayer.getName() + "&7: " + message, false);
        } else {
            receiverPlayer.sendMessage("&dFrom " + senderPlayer.getRank().getColor() + "[" + senderPlayer.getRank().getName() + "] " + senderPlayer.getName() + "&7: " + message, false);
        }

        messages.put(sender.toLowerCase(), receiver);
        messages.put(receiver.toLowerCase(), sender);
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

        // Remove the net.dungeonrealms.database.connection from the hashmap
        connections.remove(channelHandlerContext.channel());
    }

    public void onConnected(Channel channel, Server server) {
        UtilLogger.info(server.getName() + " is now ONLINE.");

        // Set the channel in the server class.
        server.setChannel(channel);

        // Register the net.dungeonrealms.database.connection in the hashmap.
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

            // Send info for all interactable servers.
            for (GameServer gameServer : DRControl.getInstance().getServerManager().getGameServers()) {
                server.sendPacket(gameServer.createInfoPacket());
            }

        }
    }
}
