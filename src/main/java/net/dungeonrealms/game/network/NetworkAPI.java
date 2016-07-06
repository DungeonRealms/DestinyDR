package net.dungeonrealms.game.network;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.FriendHandler;
import net.dungeonrealms.game.mastery.AsyncUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.menus.player.ShardSelector;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.game.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.game.punish.PunishUtils;
import net.dungeonrealms.game.ui.item.GUIButton;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
import java.net.InetAddress;
import java.util.Map;
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

                    return;
                }

                if (subChannel.equals("Friends")) {
                    Bukkit.broadcastMessage("FRIENDS PACKET");
                    String msg = in.readUTF();
                    if (msg.contains("join:")) {
                        String[] content = msg.split(",");
                        String uuid = content[1];
                        String name = content[2];
                        String shard = content[3];

                        Bukkit.getOnlinePlayers().stream().filter(p -> FriendHandler.getInstance().areFriends(p, UUID.fromString(uuid))).forEach(p -> {
                            p.sendMessage(ChatColor.GREEN + name + ChatColor.YELLOW + " has joined " + ChatColor.AQUA + ChatColor.UNDERLINE + shard);
                        });
                    }

                    return;
                }


                if (subChannel.equals("Ping")) {
                    final long currentTime = System.currentTimeMillis();
                    String hostname = in.readUTF();

                    // Make sure server has access to ping //
                    Futures.addCallback(MoreExecutors.listeningDecorator(AsyncUtils.pool).submit(() -> InetAddress.getByName(hostname).isReachable(2000)), new FutureCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean isPinged) {
                            long ping = System.currentTimeMillis() - currentTime;
                            NetworkAPI.getInstance().sendNetworkMessage("DungeonRealms", "Pinged", hostname, DungeonRealms.getInstance().bungeeName, isPinged ? String.valueOf(ping) : String.valueOf(0));
                        }

                        @ParametersAreNonnullByDefault
                        public void onFailure(Throwable ignored) {
                        }
                    });

                    return;
                }

                if (subChannel.equals("Pinged")) {
                    String hostname = in.readUTF();

                    if (!ShardSelector.CACHED_PING_SHARD_BUTTONS.containsKey(hostname)) return;

                    String bungeeName = in.readUTF();
                    String ping = in.readUTF();

                    Map<String, GUIButton> map = ShardSelector.CACHED_PING_SHARD_BUTTONS.get(hostname);

                    if (!map.containsKey(bungeeName)) return;

                    String shardID = DungeonRealms.getInstance().DR_SHARDS.get(bungeeName).getShardID();
                    map.get(bungeeName).setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + shardID + ChatColor.GRAY + " (" + ping + " ms)");
                }

            } else {
                try {
                    if (subChannel.equals("IP")) {
                        String address = in.readUTF();

                        Document existingDoc = DatabaseAPI.getInstance().getDocumentFromAddress(address);

                        if (existingDoc != null) {
                            UUID uuid = UUID.fromString(((Document) existingDoc.get("info")).get("uuid", String.class));

                            if (PunishUtils.isBanned(uuid)) {
                                String bannedMessage = PunishUtils.getBannedMessage(uuid);
                                PunishUtils.kick(player.getName(), bannedMessage);

                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.BANNED_TIME, DatabaseAPI.getInstance().getValue(uuid, EnumData.BANNED_TIME), true);
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.BANNED_REASON, DatabaseAPI.getInstance().getValue(uuid, EnumData.BANNED_REASON), true);
                            }
                        }

                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.IP_ADDRESS, address, true);
                        return;
                    }

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
