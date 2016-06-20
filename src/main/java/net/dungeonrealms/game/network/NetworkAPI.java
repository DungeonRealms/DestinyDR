package net.dungeonrealms.game.network;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.game.network.bungeecord.BungeeServerTracker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.*;
import java.util.UUID;

/**
 * Created by Nick on 10/12/2015.
 */
@SuppressWarnings("unchecked")
public class NetworkAPI implements PluginMessageListener {

    static NetworkAPI instance = null;

    public static NetworkAPI getInstance() {
        if (instance == null) {
            instance = new NetworkAPI();
        }
        return instance;
    }

    public void startInitialization() {
        Utils.log.info("[NetworkAPI] Registering Outbound/Inbound BungeeCord channels...");
        Bukkit.getMessenger().registerOutgoingPluginChannel(DungeonRealms.getInstance(), "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(DungeonRealms.getInstance(), "BungeeCord", this);

        Bukkit.getMessenger().registerOutgoingPluginChannel(DungeonRealms.getInstance(), "DungeonRealms");
        Bukkit.getMessenger().registerIncomingPluginChannel(DungeonRealms.getInstance(), "DungeonRealms", this);

        BungeeServerTracker.startTask(1);
        Utils.log.info("[NetworkAPI] Finished Registering Outbound/Inbound BungeeCord channels ... OKAY!");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equalsIgnoreCase("BungeeCord") && !channel.equalsIgnoreCase("DungeonRealms")) return;

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        try {
            String subChannel = in.readUTF();

            if (channel.equalsIgnoreCase("DungeonRealms")) {
                if (subChannel.equals("Update")) {
                    UUID uuid = UUID.fromString(in.readUTF());
                    if (Bukkit.getPlayer(uuid) != null) DatabaseAPI.getInstance().requestPlayer(uuid);
                }
            } else {
                try {
                    if (subChannel.equals("PlayerCount")) {
                        String server = in.readUTF();

                        if (in.available() > 0) {
                            int online = in.readInt();

                            BungeeServerInfo serverInfo = BungeeServerTracker.getOrCreateServerInfo(server);
                            serverInfo.setOnlinePlayers(online);
                        }
                    }

                } catch (EOFException e) {
                    // Do nothing.
                } catch (IOException e) {
                    // This should never happen.
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void askPlayerCount(String server) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("PlayerCount");
            out.writeUTF(server);
        } catch (IOException e) {
            // It should not happen.
            e.printStackTrace();
            System.out.println("I/O Exception while asking for player count on server '" + server + "'.");
        }

        // OR, if you don't need to send it to a specific player

        if (Bukkit.getOnlinePlayers().size() > 0)
            ((Player) Bukkit.getOnlinePlayers().toArray()[0]).sendPluginMessage(DungeonRealms.getInstance(), "BungeeCord", b.toByteArray());
    }

    public void sendToServer(String playerName, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectOther");
        out.writeUTF(playerName);
        out.writeUTF(serverName);
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

        if (player != null)
            player.sendPluginMessage(DungeonRealms.getInstance(), "BungeeCord", out.toByteArray());
    }

    /**
     * @param channel  Type of custom Channel (actually sub)
     * @param message  Message to send.
     * @param contents Contents of the internal guts.
     * @since 1.0
     */

    public void sendNetworkMessage(String channel, String subChannel, String message, String... contents) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subChannel);
        out.writeUTF(message);

        for (String s : contents)
            out.writeUTF(s);

        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

        if (player != null)
            player.sendPluginMessage(DungeonRealms.getInstance(), channel, out.toByteArray());
    }

    /**
     * Send a player a message through the Bungee channel.
     *
     * @param playerName Player to send message to.
     * @param message    Message to send to the player specified above.
     * @apiNote Make sure to use ChatColor net.md_5.bungee.api.ChatColor!
     * @since 1.0
     */
    public void sendPlayerMessage(String playerName, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Message");
        out.writeUTF(playerName);
        out.writeUTF(message);
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null)
            player.sendPluginMessage(DungeonRealms.getInstance(), "BungeeCord", out.toByteArray());
    }
}
