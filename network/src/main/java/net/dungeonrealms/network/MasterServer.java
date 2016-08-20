package net.dungeonrealms.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.network.packet.Packet;

import java.io.IOException;

public class MasterServer
        extends Listener {

    private Server server;

    public MasterServer() throws IOException {
        server = new Server(Constants.NET_WRITE_BUFFER_SIZE, Constants.NET_READ_BUFFER_SIZE);

        server.start();
        server.addListener(this);
        server.bind(Constants.MASTER_SERVER_PORT);
    }

    public void registerListener(Listener listener) {
        server.addListener(listener);
    }

    public Server getServer() {
        return this.server;
    }

    public Kryo getKryo() {
        return server.getKryo();
    }

    public void kill() {
        if (this.server != null) this.server.stop();
    }

    public void relayPacketTCP(Packet packet) {
        for (Connection c : server.getConnections()) c.sendTCP(packet);
    }

    public void relayPacketUDP(Packet packet) {
        for (Connection c : server.getConnections()) c.sendUDP(packet);
    }

    public void connected(Connection c) {
        Log.info("Incoming connection to master server: " + c.getRemoteAddressTCP().getAddress() + ":" + c.getRemoteAddressTCP().getPort());
    }

    @Override
    public void received(Connection c, Object object) {
        if (object instanceof Packet) {
            relayPacketTCP((Packet) object);
            Log.info("Relaying packet connection: " + c.getRemoteAddressTCP().getAddress() + ":" + c.getRemoteAddressTCP().getPort());
        }
    }
}
