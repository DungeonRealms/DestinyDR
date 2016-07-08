package net.dungeonrealms.network.kryonet;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.dungeonrealms.Constants;
import net.dungeonrealms.network.kryonet.packet.Packet;

import java.io.IOException;

public class KryonetClient
        extends Listener {

    private Client client;
    private Runnable reconnector;
    private String server_ip;
    private boolean isConnected = false;

    public KryonetClient(String server_ip) {
        this.server_ip = server_ip;
        this.client = new Client();
        this.client.addListener(this);
        this.client.setKeepAliveTCP(1000);
        this.client.start();
        this.connect();
    }

    public void setReconnector(Runnable reconnector) {
        this.reconnector = reconnector;
    }

    public void addListener(Listener listener) {
        client.addListener(listener);
    }

    public Client getClient() {
        return this.client;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void connect() {
        try {
            this.client.connect(500000, server_ip, Constants.MASTER_SERVER_PORT);
            isConnected = true;
        } catch (IOException e) {
            //Log.severe(Scope.CORE, "Oh no! Failed to connect to proxy server..");
        }
    }

    public void stop() {
        if (this.client != null) {
            this.client.stop();
        }
    }

    public void sendTCP(Packet packet) {
        this.client.sendTCP(packet);
    }

    public void sendUDP(Packet packet) {
        this.client.sendUDP(packet);
    }


    public void disconnected(Connection c) {
        //Log.info(Scope.CORE, "Oh no! Lost connection from kryonet proxy server!");
        Runnable run = new DefaultReconnector();
        if (reconnector != null) run = reconnector;
        new Thread(run).start();
    }

    public class DefaultReconnector
            implements Runnable {
        public DefaultReconnector() {
        }

        public void run() {
            while (!KryonetClient.this.getClient().isConnected()) {
                try {
                  //  Log.info(Scope.CORE, "Retrying kryonet server connection....");
                    KryonetClient.this.client.reconnect();
                } catch (Exception ex) {
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException ex1) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
