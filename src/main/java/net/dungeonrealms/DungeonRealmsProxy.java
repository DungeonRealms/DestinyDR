package net.dungeonrealms;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.guild.db.GuildDatabase;
import net.dungeonrealms.game.listeners.ProxyChannelListener;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.bson.Document;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 5/31/2016
 */

public class DungeonRealmsProxy extends Plugin {

    private static DungeonRealmsProxy instance;
    public static com.mongodb.MongoClient mongoClient = null;
    public static MongoClientURI mongoClientURI = null;
    public static com.mongodb.client.MongoDatabase database = null;
    public static com.mongodb.client.MongoCollection<Document> guilds = null;

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
