package net.dungeonrealms.proxy.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.dungeonrealms.common.network.PingResponse;
import net.dungeonrealms.common.network.ServerAddress;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.ping.ServerPinger;
import net.dungeonrealms.common.network.ping.type.BungeePingResponse;
import net.dungeonrealms.network.GameClient;
import net.dungeonrealms.network.packet.type.BasicMessagePacket;
import net.dungeonrealms.proxy.DungeonRealmsProxy;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

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

                if (task.equals("AcceptLoginToken")) {
                    UUID uuid = UUID.fromString(in.readUTF());
                    String shard = in.readUTF();

                    ProxiedPlayer player = getProxy().getPlayer(uuid);

                    if (player == null) return;

                    DungeonRealmsProxy.getInstance().ACCEPTED_CONNECTIONS.add(player.getUniqueId());
                    player.connect(getProxy().getServerInfo(shard),
                            (success, throwable) -> DungeonRealmsProxy.getInstance().ACCEPTED_CONNECTIONS.remove(player.getUniqueId()));
                    return;
                }


                if (task.equals("RefuseLoginToken")) {
                    UUID uuid = UUID.fromString(in.readUTF());
                    ProxiedPlayer player = getProxy().getPlayer(uuid);

                    if (player != null && in.available() > 0) {
                        String message = in.readUTF();

                        if (message.contains("setting up") && ShardInfo.getByPseudoName(player.getServer().getInfo().getName()) == null)
                            player.sendMessage(message);
                    }
                }

                if (task.equals("MoveSessionToken")) {
                    UUID uuid = UUID.fromString(in.readUTF());
                    ProxiedPlayer player = getProxy().getPlayer(uuid);

                    Iterator<ServerInfo> optimalShardFinder = getOptimalShards().iterator();


                    while (optimalShardFinder.hasNext()) {
                        ServerInfo target = optimalShardFinder.next();

                        try {
                            PingResponse ping = new BungeePingResponse(ServerPinger.fetchData(new ServerAddress(target.getAddress().getHostName(), target.getAddress().getPort()), 500));
                            if (!ping.isOnline() || ping.getMotd().contains("offline")) {

                                if (!optimalShardFinder.hasNext()) {
                                    // CONNECT THEM TO LOBBY LOAD BALANCER //
                                    player.connect(getProxy().getServerInfo("Lobby"));
                                    return;
                                }

                                continue;
                            }
                        } catch (Exception e) {

                            if (!optimalShardFinder.hasNext()) {
                                // CONNECT THEM TO LOBBY LOAD BALANCER //
                                player.connect(getProxy().getServerInfo("Lobby"));
                                return;
                            }

                            continue;
                        }

                        if (target.canAccess(player) && !(player.getServer() != null && player.getServer().getInfo().equals(target))) {
                            try {
                                player.connect(target);
                            } catch (Exception e) {
                                if (!optimalShardFinder.hasNext()) {
                                    // CONNECT THEM TO LOBBY LOAD BALANCER //
                                    player.connect(getProxy().getServerInfo("Lobby"));
                                } else continue;
                            }
                            player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "Moving your current session...");
                            break;
                        } else if (!optimalShardFinder.hasNext()) {
                            // CONNECT THEM TO LOBBY LOAD BALANCER //
                            player.connect(getProxy().getServerInfo("Lobby"));
                            return;
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private ProxyServer getProxy() {
        return DungeonRealmsProxy.getInstance().getProxy();
    }

    public List<ServerInfo> getOptimalShards() {
        List<ServerInfo> servers = new ArrayList<>();

        for (ShardInfo shardInfo : ShardInfo.values()) {
            // We want to only put them on a US as they may fail the criteria for another shard.
            // They are free to join another shard once connected.

            String name = shardInfo.getPseudoName();
            if (name.startsWith("us") && !name.equalsIgnoreCase("us0"))
                servers.add(getProxy().getServerInfo(name));
        }

        Collections.sort(servers, (o1, o2) -> o1.getPlayers().size() - o2.getPlayers().size());
        return servers;
    }

}
