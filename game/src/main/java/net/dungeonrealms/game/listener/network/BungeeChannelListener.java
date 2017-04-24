package net.dungeonrealms.game.listener.network;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.punishment.PunishAPI;
import net.dungeonrealms.common.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

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

        BungeeServerTracker.startTask(3L);
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


                    SQLDatabaseAPI.getInstance().executeQuery(QueryType.SELECT_BANNED_IPS.getQuery(address), rs -> {
                        try {
                            if (rs != null && rs.first()) {
                                long expiration = rs.getLong("expiration");
                                if(expiration > System.currentTimeMillis()){
                                    //Still bnaned???
                                    PunishAPI.ban(player.getUniqueId(), player.getName(), 0, -1, "Ban evading", null);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
//                    DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.IP_ADDRESS, address, true);
//                    DatabaseAPI.getInstance().retrieveDocumentFromAddress(address, existingDoc -> {
//                        if (existingDoc != null) {
//                            UUID uuid = UUID.fromString(((Document) existingDoc.get("info")).get("uuid", String.class));
//
//                            if (PunishAPI.isBanned(uuid))
//                                PunishAPI.ban(player.getUniqueId(), player.getName(), DungeonRealms.getShard().getShardID(), -1, "Ban evading", null);
//                        }
//                    });
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

                if (subChannel.equals("GetServers")) {
                    String[] servers = in.readUTF().split(", ");

                    BungeeUtils.servers.clear();

                    for (String server : servers) {
                        BungeeUtils.servers.add(server);
                    }
                }

            } catch (Exception e) {
                // This should never happen.
                e.printStackTrace();
            }
        } catch (IOException e)

        {
            e.printStackTrace();
        }
    }
}