package net.dungeonrealms.control.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * Created by Evoltr on 11/15/2016.
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // Get the channel pipeline.
        ChannelPipeline pipeline = socketChannel.pipeline();

        // Add the object decoders.
        pipeline.addLast(new ObjectEncoder());
        pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));

        pipeline.addLast(new ServerHandler());
    }
}
