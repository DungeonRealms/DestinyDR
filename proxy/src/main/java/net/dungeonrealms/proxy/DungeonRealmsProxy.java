package net.dungeonrealms.proxy;

import com.esotericsoftware.minlog.Log;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.dungeonrealms.Constants;
import net.dungeonrealms.network.GameClient;
import net.dungeonrealms.network.PingResponse;
import net.dungeonrealms.network.ServerAddress;
import net.dungeonrealms.network.ShardInfo;
import net.dungeonrealms.network.ping.ServerPinger;
import net.dungeonrealms.network.ping.type.BungeePingResponse;
import net.dungeonrealms.proxy.network.NetworkClientListener;
import net.dungeonrealms.proxy.network.ProxyChannelListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class written by APOLLOSOFTWARE.IO on 5/31/2016
 */

public class DungeonRealmsProxy extends Plugin implements Listener {

    private static DungeonRealmsProxy instance;

    public static DungeonRealmsProxy getInstance() {
        return instance;
    }

    @Getter
    private static GameClient client;

    public List<UUID> ACCEPTED_CONNECTIONS = new CopyOnWriteArrayList<>();


    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("DungeonRealmsProxy onEnable() ... STARTING UP");
        this.getProxy().getPluginManager().registerListener(this, ProxyChannelListener.getInstance());
        this.getProxy().getPluginManager().registerListener(this, this);


        getLogger().info("Connecting to DungeonRealms master server...");
        client = new GameClient();

        try {
            client.connect();
            Log.set(Log.LEVEL_INFO);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new NetworkClientListener().startInitialization(client);

        // REGISTER DUNGEON REALM SHARDS //
        Arrays.asList(ShardInfo.values()).stream().forEach(info -> {
                    ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(info.getPseudoName(), new InetSocketAddress(info.getAddress().getAddress(), info.getAddress().getPort()), "", false);
                    ProxyServer.getInstance().getServers().put(info.getPseudoName(), serverInfo);
                }
        );
    }


    @EventHandler
    public void onShardConnect(ServerConnectEvent event) {
        ShardInfo shard = ShardInfo.getByPseudoName(event.getTarget().getName());
        if (shard == null) return;

        if (ACCEPTED_CONNECTIONS.contains(event.getPlayer().getUniqueId())) return;
        event.setCancelled(true);

        // SEND REQUEST PLAYER'S DATA PACKET //
        sendPacket("LoginRequestToken", event.getPlayer().getUniqueId().toString(), shard.getPseudoName());
    }


    public static void sendPacket(String task, String... contents) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(task);

        for (String s : contents)
            out.writeUTF(s);

        getClient().sendTCP(out.toByteArray());
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent ev) {
        if (!(ev.getCursor().startsWith("/") || ev.getCursor().startsWith("@")))
            return;

        String partialPlayerName = ev.getCursor().toLowerCase();

        int lastSpaceIndex = partialPlayerName.lastIndexOf(' ');
        if (lastSpaceIndex >= 0) partialPlayerName = partialPlayerName.substring(lastSpaceIndex + 1);

        for (ProxiedPlayer p : getProxy().getPlayers())
            if (p.getName().toLowerCase().startsWith(partialPlayerName)) ev.getSuggestions().add(p.getName());
    }


    @EventHandler
    public void onProxyConnection(PreLoginEvent event) {
        // DUPE GLITCH FIX //
        getProxy().getPlayers().stream().filter(p -> p.getUniqueId().equals(event.getConnection().getUniqueId())).forEach(p -> {
            if (p != null)
                p.disconnect(ChatColor.RED + "Another player with your account has logged into the server!");

            event.setCancelReason(ChatColor.RED + "Another player with your account has logged into the server!");
            event.setCancelled(true);
        });
    }


    @EventHandler
    public void onPing(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();

        int players = ping.getPlayers().getOnline();
        ServerPing.PlayerInfo[] sample = ping.getPlayers().getSample();

        ping.setDescription(ChatColor.translateAlternateColorCodes('&', Constants.MOTD));
        ping.setPlayers(new ServerPing.Players(Constants.PLAYER_SLOTS, players, sample));
    }


    // RIP LOAD BALANCER //
    //@EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if ((event.getPlayer().getServer() == null) || event.getTarget().getName().equals("Lobby")) {
            Iterator<ServerInfo> optimalShardFinder = getOptimalShards().iterator();
            event.getPlayer().sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Finding an available shard for you...");

            while (optimalShardFinder.hasNext()) {
                ServerInfo target = optimalShardFinder.next();

                try {
                    PingResponse data = new BungeePingResponse(ServerPinger.fetchData(new ServerAddress(target.getAddress().getHostName(), target.getAddress().getPort()), 700));
                    if (!data.isOnline() || data.getMotd().equals("offline")) {

                        if (!optimalShardFinder.hasNext()) {
                            event.getPlayer().disconnect(ChatColor.RED + "Could not find an optimal shard for you.. Please try again later.");
                            return;
                        }

                        continue;
                    }
                } catch (Exception e) {

                    if (!optimalShardFinder.hasNext()) {
                        event.getPlayer().disconnect(ChatColor.RED + "Could not find an optimal shard for you.. Please try again later.");
                        return;
                    }

                    continue;
                }

                if (target.canAccess(event.getPlayer()) && !(event.getPlayer().getServer() != null && event.getPlayer().getServer().getInfo().equals(target))) {
                    try {
                        event.setTarget(target);

                    } catch (Exception e) {
                        if (!optimalShardFinder.hasNext())
                            event.getPlayer().disconnect(ChatColor.RED + "Could not find an optimal shard for you.. Please try again later.");
                    }

                    break;
                } else if (!optimalShardFinder.hasNext()) {
                    event.getPlayer().disconnect(ChatColor.RED + "Could not find an optimal shard for you.. Please try again later.");
                    return;
                }
            }
        }
    }

    public List<ServerInfo> getOptimalShards() {
        List<ServerInfo> servers = new ArrayList<>();

//        for (String shardName : LOAD_BALANCED_SHARDS)
//            // We want to only put them on a US as they may fail the criteria for another shard.
//            // They are free to join another shard once connected.
//            if (shardName.startsWith("us") && !shardName.equalsIgnoreCase("us0"))
//                servers.add(getProxy().getServerInfo(shardName));

        Collections.sort(servers, (o1, o2) -> o1.getPlayers().size() - o2.getPlayers().size());
        return servers;
    }
}