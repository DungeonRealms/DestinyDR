package net.dungeonrealms.common.network.bungeecord;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/13/2016
 */
public class BungeeUtils {

    @Setter
    private static JavaPlugin plugin;

    public static void askPlayerCount(String server) {
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
            ((Player) Bukkit.getOnlinePlayers().toArray()[0]).sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
    }

    public static void sendToServer(String playerName, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectOther");
        out.writeUTF(playerName);
        out.writeUTF(serverName);
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

        if (player != null)
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    /**
     * @param channel  Type of custom Channel (actually sub)
     * @param message  Message to send.
     * @param contents Contents of the internal guts.
     * @since 1.0
     */

    public static void sendNetworkMessage(String channel, String subChannel, String message, String... contents) {
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

        if (player != null)
            sendNetworkMessage(channel, subChannel, message, contents);
    }

    /**
     * @param player   Player
     * @param channel  Type of custom Channel (actually sub)
     * @param message  Message to send.
     * @param contents Contents of the internal guts.
     * @since 1.0
     */

    public static void sendNetworkMessage(Player player, String channel, String subChannel, String message, String... contents) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subChannel);
        out.writeUTF(message);

        for (String s : contents)
            out.writeUTF(s);
        player.sendPluginMessage(plugin, channel, out.toByteArray());
    }

    /**
     * Send a player a message through the Bungee channel.
     *
     * @param playerName Player to send message to.
     * @param message    Message to send to the player specified above.
     * @apiNote Make sure to use ChatColor net.md_5.bungee.api.ChatColor!
     * @since 1.0
     */
    public static void sendPlayerMessage(String playerName, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Message");
        out.writeUTF(playerName);
        out.writeUTF(message);
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null)
            player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }
}
