package net.dungeonrealms.bukkit.managers;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Setter;
import net.dungeonrealms.bukkit.BukkitCore;
import net.dungeonrealms.bukkit.netty.ClientInitializer;
import net.dungeonrealms.packet.Packet;
import net.dungeonrealms.packet.connect.PacketConnect;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by Evoltr on 12/3/2016.
 */
public class NetworkManager implements Listener {

    private BukkitCore plugin;

    private EventLoopGroup group = new NioEventLoopGroup();
    private Bootstrap bootstrap = new Bootstrap();

    @Setter
    private Channel channel = null;

    public NetworkManager(BukkitCore plugin) {
        this.plugin = plugin;

        this.bootstrap.group(group);
        this.bootstrap.channel(NioSocketChannel.class);
        this.bootstrap.handler(new ClientInitializer());
        this.bootstrap.remoteAddress(plugin.getConfig().getString("control-ip"), 8192);

        // Reconnect every 5 seconds.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isConnected()) {
                    connect();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 100L);
    }

    public void connect() {
        BukkitCore.log("Connecting to DRControl...");

        try {
            channel = bootstrap.connect().sync().channel();

            // Send login packet to authenticate ourselves.
            channel.writeAndFlush(new PacketConnect("SERVER", plugin.getServerName()));

            BukkitCore.log("Connection with DRControl established.");
        } catch (Exception e) {
            BukkitCore.log("Failed to connect to DRControl: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            channel.close().sync();
        } catch (Exception e) {
            //Ignore.
        }
        group.shutdownGracefully();
    }

    public void sendPacket(Packet packet) throws Exception {
        if (!isConnected()) {
            throw new Exception("Not connected to DRControl!");
        }
        BukkitCore.log("Sending packet " + packet.toString());
        channel.writeAndFlush(packet);
    }

    public boolean isConnected() {
        return channel != null;
    }

}
