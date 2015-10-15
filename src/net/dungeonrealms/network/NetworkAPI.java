package net.dungeonrealms.network;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumGuildData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.ArrayList;
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
        Utils.log.info("[NetworkAPI] Finished Registering Outbound/Inbound BungeeCord channels ... OKAY!");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equalsIgnoreCase("BungeeCord")) return;
        switch (channel) {
            case "":
                break;
            default:
        }
    }

    /**
     * Send a player a message through the Bungee channel.
     *
     * @param playerName
     * @param message
     * @apiNote Make sure to use ChatColor net.md_5.bungee.api.ChatColor!
     * @since 1.0
     */
    public void sendPlayerMessage(String playerName, String message) {
        if (Bukkit.getOnlinePlayers().size() <= 0) return;
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Message");
        out.writeUTF(playerName);
        out.writeUTF(message);
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        assert player != null : "sendPlayerMessage() NetworkAPI.java unable to find Iterables.first(Player)";
        player.sendPluginMessage(DungeonRealms.getInstance(), "BungeeCord", out.toByteArray());
    }

    /**
     * Sends a players in a guild a message.
     * ATM: only works per-shard.
     *
     * @param guildName
     * @param message
     * @since 1.0
     */
    public void sendAllGuildMessage(String guildName, String message) {
        ArrayList<String> members = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumGuildData.MEMBERS, guildName);
        ArrayList<String> officers = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumGuildData.OFFICERS, guildName);
        members.addAll(officers);
        members.add((String) DatabaseAPI.getInstance().getData(EnumGuildData.OWNER, guildName));
        members.add((String) DatabaseAPI.getInstance().getData(EnumGuildData.CO_OWNER, guildName));

        members.stream().filter(s -> s != null && !s.equals("") && API.isOnline(UUID.fromString(s))).forEach(s -> {
            Bukkit.getPlayer(UUID.fromString(s)).sendMessage("[" + ChatColor.GREEN.toString() + ChatColor.BOLD + guildName + ChatColor.RESET + "]" + " " + message);
        });

    }

}
