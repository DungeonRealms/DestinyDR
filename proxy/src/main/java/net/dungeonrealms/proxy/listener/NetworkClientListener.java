package net.dungeonrealms.proxy.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.dungeonrealms.network.GameClient;
import net.dungeonrealms.network.packet.type.BasicMessagePacket;
import net.dungeonrealms.proxy.DungeonRealmsProxy;
import net.md_5.bungee.api.ProxyServer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/15/2016
 */
public class NetworkClientListener extends Listener {

    public void startInitialization(GameClient client) {
        if (client == null) return;

        DungeonRealmsProxy.getInstance().getLogger().info("[NetworkClientListener] Registering client packet listener...");
        client.registerListener(this);
    }


    public void stopInvocation(GameClient client) {
        if (client == null) return;

        DungeonRealmsProxy.getInstance().getLogger().info("[NetworkClientListener] Unregistering client packet listener...");
        client.removeListener(this);
    }


    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof BasicMessagePacket) {
            BasicMessagePacket packet = (BasicMessagePacket) object;

            byte[] data = packet.data;
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

            try {
                String task = in.readUTF();

                if (task.equals("MoveSessionToken")) {
                    UUID uuid = UUID.fromString(in.readUTF());
                    boolean sub = Boolean.valueOf(in.readUTF());

                    DungeonRealmsProxy.getInstance().LoadBalancer(uuid, false, sub, true);

                } else if (task.equals("HandleNewAccount")) {
                    UUID uuid = UUID.fromString(in.readUTF());
                    DungeonRealmsProxy.getInstance().LoadBalancer(uuid, true, false, false);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private ProxyServer getProxy() {
        return DungeonRealmsProxy.getInstance().getProxy();
    }


}
