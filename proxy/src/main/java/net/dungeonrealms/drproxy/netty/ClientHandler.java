package net.dungeonrealms.drproxy.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.dungeonrealms.drproxy.DRProxy;
import net.dungeonrealms.drproxy.player.rank.Rank;
import net.dungeonrealms.packet.Packet;
import net.dungeonrealms.packet.network.PacketMOTD;
import net.dungeonrealms.packet.network.PacketPlayerCount;
import net.dungeonrealms.packet.network.PacketRestart;
import net.dungeonrealms.packet.network.PacketStaffMessage;
import net.dungeonrealms.packet.player.PacketMessage;
import net.dungeonrealms.packet.player.PacketPlayerConnect;
import net.dungeonrealms.packet.server.PacketServerInfo;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.Chat;

/**
 * Created by Evoltr on 11/30/2016.
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private NettyHandler nettyHandler;

    public ClientHandler(NettyHandler nettyHandler) {
        this.nettyHandler = nettyHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) {

        // Convert the received message into a packet.
        Packet packet = (Packet) obj;

        // Handle the motd packet.
        if (packet instanceof PacketMOTD) {
            PacketMOTD packetMOTD = (PacketMOTD) packet;

            if (packetMOTD.getMOTD() != null) {
                nettyHandler.setMOTD(ChatColor.translateAlternateColorCodes('&', packetMOTD.getMOTD()));
            }
        }

        // Handle the player count packet.
        if (packet instanceof PacketPlayerCount) {
            PacketPlayerCount packetPlayerCount = (PacketPlayerCount) packet;

            // Update the player count in the netty handler.
            nettyHandler.setOnlinePlayers(packetPlayerCount.getOnlinePlayers());
            nettyHandler.setMaxPlayers(packetPlayerCount.getMaxPlayers());
        }

        // Handle the staff message packet.
        if (packet instanceof PacketStaffMessage) {
            PacketStaffMessage packetStaffMessage = (PacketStaffMessage) packet;

            // Add color to the message.
            String message = ChatColor.translateAlternateColorCodes('&', packetStaffMessage.getMessage());

            // Send the message to all online staff.
            DRProxy.getInstance().getPlayerManager().getOnlinePlayers().stream().filter(networkPlayer -> networkPlayer.getRank().getID() >= Rank.PMOD.getID()).forEach(networkPlayer -> networkPlayer.getProxiedPlayer().sendMessage(TextComponent.fromLegacyText(message)));
        }

        // Handle the message packet.
        if (packet instanceof PacketMessage) {
            PacketMessage packetMessage = (PacketMessage) packet;

            // Add color to the message.
            String message = ChatColor.translateAlternateColorCodes('&', packetMessage.getMessage());

            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packetMessage.getPlayer());

            // Send the message to the player.
            if (player != null) {

                if (message.startsWith("{") && message.endsWith("}")) {
                    player.unsafe().sendPacket(new Chat(message));
                } else {
                    player.sendMessage(TextComponent.fromLegacyText(message));
                }

            }
        }

        // Handle the restart packet.
        if (packet instanceof PacketRestart) {
            ProxyServer.getInstance().stop();
        }

        // Handle the player connect packet.
        if (packet instanceof PacketPlayerConnect) {
            PacketPlayerConnect packetPlayerConnect = (PacketPlayerConnect) packet;

            String player = packetPlayerConnect.getPlayer();
            String server = packetPlayerConnect.getServer();

            ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(player);
            ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(server);

            if (proxiedPlayer != null && serverInfo != null) {
                proxiedPlayer.connect(serverInfo);
            }
        }

        // Handle the server info packet.
        if (packet instanceof PacketServerInfo) {
            nettyHandler.updateServer((PacketServerInfo) packet);
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        DRProxy.log("Lost connection to DRControl!");

        // Remove the connection from the network manager.
        nettyHandler.setChannel(null);
    }

}
