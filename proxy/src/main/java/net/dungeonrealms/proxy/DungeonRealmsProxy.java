package net.dungeonrealms.proxy;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import net.dungeonrealms.Constants;
import net.dungeonrealms.network.PingResponse;
import net.dungeonrealms.network.ServerAddress;
import net.dungeonrealms.network.ShardInfo;
import net.dungeonrealms.network.ping.ServerPinger;
import net.dungeonrealms.network.ping.method.BungeePingResponse;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * Class written by APOLLOSOFTWARE.IO on 5/31/2016
 */

public class DungeonRealmsProxy extends Plugin implements Listener {

    private final String[] LOAD_BALANCED_SHARDS = new String[]{"us1", "us2", "us3", "us4", "us5"}; // @note: don't include special shards

    public static com.mongodb.MongoClient mongoClient = null;
    public static MongoClientURI mongoClientURI = null;
    public static com.mongodb.client.MongoDatabase database = null;
    public static com.mongodb.client.MongoCollection<Document> guilds = null;
    private static DungeonRealmsProxy instance;

    public static DungeonRealmsProxy getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("DungeonRealmsProxy onEnable() ... STARTING UP");
        getLogger().info("DungeonRealmsProxy Starting [MONGODB] Connection...");
        mongoClientURI = new MongoClientURI("mongodb://dungeonuser:mwH47e552qxWPwxL@ds025224-a0.mlab.com:25224,ds025224-a1.mlab.com:25224/dungeonrealms?replicaSet=rs-ds025224");
        mongoClient = new MongoClient(mongoClientURI);
        database = mongoClient.getDatabase("dungeonrealms");
        guilds = database.getCollection("guilds");
        getLogger().info("DungeonRealmsProxy [MONGODB] has connected successfully!");

        this.getProxy().getPluginManager().registerListener(this, ProxyChannelListener.getInstance());
        this.getProxy().getPluginManager().registerListener(this, this);

        // ADD DUNGEON REALM SHARDS //
        Arrays.asList(ShardInfo.values()).stream().forEach(info -> {
                    ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(info.getPseudoName(), new InetSocketAddress(info.getAddress().getAddress(), info.getAddress().getPort()), "", false);
                    ProxyServer.getInstance().getServers().put(info.getPseudoName(), serverInfo);
                }
        );
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

    public List<ServerInfo> getOptimalShards() {
        List<ServerInfo> servers = new ArrayList<>();

        for (String shardName : LOAD_BALANCED_SHARDS)
            // We want to only put them on a US as they may fail the criteria for another shard.
            // They are free to join another shard once connected.
            if (shardName.startsWith("us") && !shardName.equalsIgnoreCase("us0"))
                servers.add(getProxy().getServerInfo(shardName));

        Collections.sort(servers, (o1, o2) -> o1.getPlayers().size() - o2.getPlayers().size());

        return servers;
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

    public void relayPacket(String channel, byte[] data) {
        for (ServerInfo server : ProxyServer.getInstance().getServers().values())
            server.sendData(channel, data);
    }

}
