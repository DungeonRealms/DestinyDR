package net.dungeonrealms;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.guild.db.GuildDatabase;
import net.dungeonrealms.game.listeners.ProxyChannelListener;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.bson.Document;

import java.util.*;

/**
 * Class written by APOLLOSOFTWARE.IO on 5/31/2016
 */

public class DungeonRealmsProxy extends Plugin implements Listener {

    private static DungeonRealmsProxy instance;
    public static com.mongodb.MongoClient mongoClient = null;
    public static MongoClientURI mongoClientURI = null;
    public static com.mongodb.client.MongoDatabase database = null;
    public static com.mongodb.client.MongoCollection<Document> guilds = null;

    private final String[] DR_SHARDS = new String[]{"dr1", "dr2"};

    private List<String> trackedShards = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("DungeonRealmsProxy onEnable() ... STARTING UP");
        getLogger().info("DungeonRealms Starting [MONGODB] Connection...");
        mongoClientURI = new MongoClientURI("mongodb://104.236.116.27:27017/dungeonrealms");
        mongoClient = new MongoClient(mongoClientURI);
        database = mongoClient.getDatabase("dungeonrealms");

        getLogger().info("[GUILDS] Pull guilds from database...");
        guilds = database.getCollection("guilds");
        GuildDatabase.setGuilds(guilds);

        getLogger().info("DungeonRealms [MONGODB] has connected successfully!");
        this.getProxy().getPluginManager().registerListener(this, ProxyChannelListener.getInstance());
        this.getProxy().getPluginManager().registerListener(this, this);
    }

    public List<ServerInfo> getOptimalShards() {
        List<ServerInfo> server = new ArrayList<>();

        for (String shardName : DR_SHARDS) server.add(getProxy().getServerInfo(shardName));
        Collections.sort(server, (o1, o2) -> o1.getPlayers().size() - o2.getPlayers().size());
        Collections.reverse(server);

        return server;
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        if ((event.getPlayer().getServer() == null) || event.getTarget().getName().equals("Lobby")) {
            Iterator<ServerInfo> optimalShardFinder = getOptimalShards().iterator();

            event.getPlayer().sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Finding an available shard for you...");

            optimalShardFinder.forEachRemaining(target -> {
                if (target.canAccess(event.getPlayer()) && !(event.getPlayer().getServer() != null && event.getPlayer().getServer().getInfo().equals(target))) {
                    try {
                        event.setTarget(target);
                    } catch (Exception e) {
                        if (!optimalShardFinder.hasNext())
                            event.getPlayer().disconnect(ChatColor.RED + "Could not find an optimal shard for you.. Please try again later.");
                    }
                }
            });
        }
    }

    public void sendMessageToGuild(String guildName, String message, String... filters) {
        loop:
        for (UUID uuid : GuildDatabaseAPI.get().getAllOfGuild(guildName)) {
            ProxiedPlayer player = getProxy().getPlayer(uuid);

            if (player != null) {
                for (String s : filters) {
                    if (player.getName().equalsIgnoreCase(s))
                        continue loop;
                }
                player.sendMessage(message);
            }
        }
    }

    public void relayPacket(String channel, byte[] data) {
        for (ServerInfo server : ProxyServer.getInstance().getServers().values())
            server.sendData(channel, data);
    }

    public static DungeonRealmsProxy getInstance() {
        return instance;
    }
}
