package net.dungeonrealms.proxy;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import net.dungeonrealms.network.PingResponse;
import net.dungeonrealms.network.ServerAddress;
import net.dungeonrealms.network.ping.ServerPinger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Class written by APOLLOSOFTWARE.IO on 5/31/2016
 */

public class DungeonRealmsProxy extends Plugin implements Listener {

    private final String[] DR_SHARDS = new String[]{"us1", "us2", "us3", "sub1"}; // @note: don't include special shards

    public static com.mongodb.MongoClient mongoClient = null;
    public static MongoClientURI mongoClientURI = null;
    public static com.mongodb.client.MongoDatabase database = null;
    public static com.mongodb.client.MongoCollection<Document> guilds = null;
    private static DungeonRealmsProxy instance;

    public static DungeonRealmsProxy getInstance() {
        return instance;
    }

    private final String MOTD;
    private final int MAX_PLAYERS;

    public DungeonRealmsProxy() {
        MOTD = "&6Dungeon Realms &8- &7&aNow available on v1.9 & v1.10!              &7Open Beta Weekend     &8-&f&nwww.dungeonrealms.net &8-";
        MAX_PLAYERS = 500;
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
    }

    @EventHandler
    public void onProxyConnection(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        final int[] count = {0};

        // DUPE GLITCH FIX //
        getProxy().getScheduler().runAsync(this, () -> getProxy().getPlayers().stream().filter(p -> p.getUniqueId().equals(player.getUniqueId())).forEach(p -> {
            count[0]++;

            if (count[0] >= 2) {
                if (player != null) {
                    getProxy().getScheduler().schedule(DungeonRealmsProxy.this,
                            () -> player.disconnect(ChatColor.RED + "Another player with your account has logged into the server!"), 1, TimeUnit.NANOSECONDS);

                }
                getProxy().getScheduler().schedule(DungeonRealmsProxy.this,
                        () -> p.disconnect(ChatColor.RED + "Another player with your account has logged into the server!"), 1, TimeUnit.NANOSECONDS);
            }
        }));
    }

    @EventHandler
    public void onPing(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();

        int players = ping.getPlayers().getOnline();
        ServerPing.PlayerInfo[] sample = ping.getPlayers().getSample();

        ping.setDescription(ChatColor.translateAlternateColorCodes('&', MOTD));
        ping.setPlayers(new ServerPing.Players(MAX_PLAYERS, players, sample));
    }

    public List<ServerInfo> getOptimalShards() {
        List<ServerInfo> servers = new ArrayList<>();

        for (String shardName : DR_SHARDS)
            // We want to only put them on a US as they may fail the criteria for another shard.
            // They are free to join another shard once connected.
            if (shardName.startsWith("us") && !shardName.equalsIgnoreCase("us0"))
                servers.add(getProxy().getServerInfo(shardName));

        Collections.sort(servers, (o1, o2) -> o1.getPlayers().size() - o2.getPlayers().size());

        return servers;
    }

    @EventHandler
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
//
//    public void sendMessageToGuild(String guildName, String message, String... filters) {
//        loop:
//        for (UUID uuid : GuildDatabaseAPI.get().getAllOfGuild(guildName)) {
//            ProxiedPlayer player = getProxy().getPlayer(uuid);
//
//            if (player != null) {
//                for (String s : filters)
//                    if (player.getName().equalsIgnoreCase(s))
//                        continue loop;
//                player.sendMessage(message);
//            }
//        }
//    }

    public void relayPacket(String channel, byte[] data) {
        for (ServerInfo server : ProxyServer.getInstance().getServers().values())
            server.sendData(channel, data);
    }

}
