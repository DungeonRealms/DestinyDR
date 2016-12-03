package net.dungeonrealms.bukkit.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import net.dungeonrealms.bukkit.BukkitCore;

/**
 * Created by Evoltr on 12/3/2016.
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        // Get the channel pipeline.
        ChannelPipeline pipeline = socketChannel.pipeline();

        // Add object decoders.
        pipeline.addLast(new ObjectEncoder());
        pipeline.addLast(new ObjectDecoder(ClassResolvers.weakCachingResolver(BukkitCore.class.getClassLoader())));

        // Add client handler.
        pipeline.addLast(new ClientHandler());
    }
}
