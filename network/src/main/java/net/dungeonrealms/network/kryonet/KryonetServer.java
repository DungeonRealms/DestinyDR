package net.dungeonrealms.network.kryonet;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import net.dungeonrealms.Constants;
import net.dungeonrealms.network.kryonet.packet.Packet;

import java.io.IOException;

public class KryonetServer
        extends Listener {

    private Server server;
    public KryonetServer() throws IOException {

        int port = Constants.MASTER_SERVER_PORT;
        server = new com.esotericsoftware.kryonet.Server();
        server.start();
        server.addListener(this);
        //Core.registerKryoClasses(server.getKryo());
        server.bind(port);
        //Log.info(Scope.CORE, "A kryonet server has been initiated binded to: /127.0.0.1:" + port);
    }

    public void addListener(Listener listener) {
        server.addListener(listener);
    }

    public Server getServer() {
        return this.server;
    }

    public void stop() {
        if (this.server != null) {
            this.server.stop();
        }
    }

    public void sendPacketToAllConnections(Packet packet) {
        for (Connection c : server.getConnections()) c.sendTCP(packet);
    }

    public void sendUDPToAllConnections(Packet packet) {
        for (Connection c : server.getConnections()) {
            c.sendUDP(packet);
        }
    }

    public void connected(Connection c) {
       // Log.info(Scope.CORE, "Client connected to proxy: " + c.getRemoteAddressTCP().getAddress() + ":" + c.getRemoteAddressTCP().getPort());
    }

    public void received(Connection c, Object o) {


    }
}
