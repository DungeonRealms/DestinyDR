package net.dungeonrealms.lobby.bungee;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.dungeonrealms.lobby.Lobby;
import net.dungeonrealms.network.GameClient;
import net.dungeonrealms.network.packet.type.BasicMessagePacket;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkClientListener extends Listener {
    public void startInitialization(GameClient client) {
        if (client == null) return;

        Lobby.getInstance().getLogger().info("[NetworkClientListener] Registering client packet listener...");
        client.registerListener(this);
    }


    public void stopInvocation(GameClient client) {
        if (client == null) return;

        Lobby.getInstance().getLogger().info("[NetworkClientListener] Unregistering client packet listener...");
        client.removeListener(this);
    }


    private SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof BasicMessagePacket) {
            BasicMessagePacket packet = (BasicMessagePacket) object;

            byte[] data = packet.data;
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));

            try {
                String task = in.readUTF();

                if (task.equals("playerLogout")) {
                    UUID uuid = UUID.fromString(in.readUTF());
                    String time = in.readUTF();

                    Lobby.getInstance().getRecentLogouts().put(uuid, new AtomicInteger(Integer.parseInt(time)));
//                    Bukkit.getLogger().info("Received " + uuid.toString() + " from " + time);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
