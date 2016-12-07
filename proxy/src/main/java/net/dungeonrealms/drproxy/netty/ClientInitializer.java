package net.dungeonrealms.drproxy.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import net.dungeonrealms.drproxy.DRProxy;

/**
 * Created by Evoltr on 11/30/2016.
 */
public class ClientInitializer extends ChannelInitializer<SocketChannel> {

    private NettyHandler nettyHandler;

    public ClientInitializer(NettyHandler nettyHandler) {
        this.nettyHandler = nettyHandler;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // Get the channel pipeline.
        ChannelPipeline pipeline = socketChannel.pipeline();

        //Add the object decoders.
        pipeline.addLast(new ObjectEncoder());
        pipeline.addLast(new ObjectDecoder(ClassResolvers.weakCachingResolver(DRProxy.class.getClassLoader())));

        // Add the client handler.
        pipeline.addLast(new ClientHandler(nettyHandler));
    }
}
