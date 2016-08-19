package net.dungeonrealms.game.listener.network;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.common.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by Nick on 10/12/2015.
 */
@SuppressWarnings("unchecked")
public class BungeeChannelListener implements PluginMessageListener, GenericMechanic {

    static BungeeChannelListener instance = null;

    public static BungeeChannelListener getInstance() {
        if (instance == null) {
            instance = new BungeeChannelListener();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    public void startInitialization() {
        Utils.log.info("[BungeeChannelListener] Registering Outbound/Inbound BungeeCord channels...");
        Bukkit.getMessenger().registerOutgoingPluginChannel(DungeonRealms.getInstance(), "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(DungeonRealms.getInstance(), "BungeeCord", this);

        BungeeServerTracker.startTask(6L);
        Utils.log.info("[BungeeChannelListener] Finished Registering Outbound/Inbound BungeeCord channels ... OKAY!");
    }

    @Override
    public void stopInvocation() {
        Utils.log.info("[BungeeChannelListener] Unregistering Outbound/Inbound BungeeCord channels...");

        Bukkit.getMessenger().unregisterIncomingPluginChannel(DungeonRealms.getInstance(), "BungeeCord");
        Bukkit.getMessenger().unregisterIncomingPluginChannel(DungeonRealms.getInstance(), "BungeeCord", this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equalsIgnoreCase("BungeeCord")) return;

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        try {
            String subChannel = in.readUTF();

            try {
                if (subChannel.equals("IP")) {
                    String address = in.readUTF();

                    GameAPI.submitAsyncCallback(() -> DatabaseAPI.getInstance().getDocumentFromAddress(address),
                            consumer -> {
                                try {
                                    Document existingDoc = consumer.get();
                                    if (existingDoc != null) {
                                        UUID uuid = UUID.fromString(((Document) existingDoc.get("info")).get("uuid", String.class));

                                        if (PunishAPI.isBanned(uuid))
                                            PunishAPI.ban(player.getUniqueId(), player.getName(), DungeonRealms.getShard().getShardID(), -1, "Ban evading", null);
                                    }
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }

                            });

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
        } catch (
                IOException e
                )

        {
            e.printStackTrace();
        }
    }
}