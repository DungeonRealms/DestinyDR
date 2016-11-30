package net.dungeonrealms.drproxy.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.dungeonrealms.drproxy.DRProxy;
import net.dungeonrealms.drproxy.netty.ClientInitializer;
import net.dungeonrealms.drproxy.player.NetworkPlayer;
import net.dungeonrealms.packet.Packet;
import net.dungeonrealms.packet.connect.PacketConnect;
import net.dungeonrealms.packet.player.PacketPlayerJoin;
import net.dungeonrealms.packet.server.PacketServerInfo;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Evoltr on 11/30/2016.
 */
public class NettyHandler implements Listener {

    private DRProxy plugin;

    private Map<String, PacketServerInfo> servers = new HashMap<>();

    private EventLoopGroup group = new NioEventLoopGroup();
    private Bootstrap bootstrap = new Bootstrap();

    private int onlinePlayers = 0;
    private int maxPlayers = 0;

    private String motd = "";

    private Channel channel = null;

    public NettyHandler(DRProxy plugin) {
        this.plugin = plugin;
        this.plugin.getProxy().getPluginManager().registerListener(plugin, this);

        this.bootstrap.group(group);
        this.bootstrap.channel(NioSocketChannel.class);
        this.bootstrap.handler(new ClientInitializer(this));
        this.bootstrap.remoteAddress(plugin.getConfig().getString("control-ip"), 8192);

        // Check we're connected every 5 seconds.
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            if (!isConnected()) {
                connect();
            }
        }, 0, 5, TimeUnit.SECONDS);

        // Remove all servers from the bungee config. These will be loaded from DRControl.
        plugin.getProxy().getServers().clear();

    }

    public void connect() {
        DRProxy.log("Connecting to DRControl....");

        try {
            channel = bootstrap.connect().sync().channel();

            // Send login packet to authenticate ourselves.
            channel.writeAndFlush(new PacketConnect("PROXY", plugin.getProxyName()));

            // Send login packet for all online players.
            for (NetworkPlayer player : DRProxy.getInstance().getPlayerManager().getOnlinePlayers()) {
                sendPacket(new PacketPlayerJoin(player.getUUID(), player.getName(), player.getRank().getName()));
            }

            DRProxy.log("Connection to DRControl established.");

        } catch (Exception e) {
            DRProxy.log("Failed to connect to DRControl: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            channel.close().sync();
        } catch (Exception e) {
            // Ignore.
        }

        group.shutdownGracefully();
    }


    public boolean isConnected() {
        return channel != null;
    }

    public void sendPacket(Packet packet) throws Exception {
        if (!isConnected()) {
            throw new Exception("Not connected to DRControl!");
        }

        channel.writeAndFlush(packet);
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getMOTD() {
        return motd;
    }

    public void setMOTD(String motd) {
        this.motd = motd;
    }

    public void updateServer(PacketServerInfo packet) {
        ProxyServer proxy = plugin.getProxy();

        // Add the server if it does not exist.
        if (proxy.getServerInfo(packet.getName()) == null) {

            String name = packet.getName();
            String host = packet.getHost();
            int port = packet.getPort();

            // Add the server to the bungee server list.
            proxy.getServers().put(name, proxy.constructServerInfo(name, new InetSocketAddress(host, port), "", false));
        }

        servers.put(packet.getName(), packet);
    }

}
