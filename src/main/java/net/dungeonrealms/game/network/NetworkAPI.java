package net.dungeonrealms.game.network;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.MailHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

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

    //TODO: Make a network message to update guilds across entire network if an even should occur.
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equalsIgnoreCase("BungeeCord")) return;
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        switch (subChannel) {
            case "mail":
                if (in.readUTF().equals("update")) {
                    Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equals(in.readUTF())).forEach(p -> {
                        DatabaseAPI.getInstance().requestPlayer(p.getUniqueId());
                        MailHandler.getInstance().sendMailMessage(p, ChatColor.GREEN + "You got mail!");
                    });
                }
                break;
            case "player":
                if (in.readUTF().equals("update")) {
                    Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equals(in.readUTF())).forEach(p -> DatabaseAPI.getInstance().requestPlayer(p.getUniqueId()));
                }
                break;
            case "shop":
                if (in.readUTF().equalsIgnoreCase("close")) {
                    Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equals(in.readUTF())).forEach(p -> {
                        if (ShopMechanics.getShop(p.getName()) != null) {
                            ShopMechanics.getShop(p.getName()).deleteShop();
                        }
                        DatabaseAPI.getInstance().update(p.getUniqueId(), EnumOperators.$SET, EnumData.HASSHOP, false, true);
                    });
                }
            default:
        }
    }

    public void sendToServer(String playerName, String serverName) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectOther");
        out.writeUTF(playerName);
        out.writeUTF(serverName);
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        player.sendPluginMessage(DungeonRealms.getInstance(), "BungeeCord", out.toByteArray());
    }

    /**
     * @param channel  Type of custom Channel (actually sub)
     * @param message  Message to send.
     * @param contents Contents of the internal guts.
     * @since 1.0
     */
    public void sendNetworkMessage(String channel, String message, String contents) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(channel);
        out.writeUTF(message);
        out.writeUTF(contents);
        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        player.sendPluginMessage(DungeonRealms.getInstance(), "BungeeCord", out.toByteArray());
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
        player.sendPluginMessage(DungeonRealms.getInstance(), "BungeeCord", out.toByteArray());
    }
}