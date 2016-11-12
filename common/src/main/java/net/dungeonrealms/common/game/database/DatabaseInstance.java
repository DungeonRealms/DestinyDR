package net.dungeonrealms.common.game.database;

import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.concurrent.MongoAccessThread;
import net.dungeonrealms.common.game.util.AsyncUtils;
import org.bson.Document;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by Nick on 8/29/2015.
 */

public class DatabaseInstance
{
    private static DatabaseInstance instance = null;

    public static DatabaseInstance getInstance()
    {
        if (instance == null)
        {
            instance = new DatabaseInstance();
        }
        return instance;
    }

    public static MongoClient mongoClient = null;
    public static MongoClientURI mongoClientURI = null;
    public static MongoDatabase database = null;

    public static List<MongoAccessThread> accessThreads;

    public static MongoCollection<Document> playerData, shardData, bans, guilds, quests;
    protected boolean cacheData = true;

    public void startInitialization(boolean cacheData)
    {
        this.cacheData = cacheData;
        mongoClientURI = new MongoClientURI(Constants.DATABASE_URI);

        Constants.log.info("DungeonRealms Database connection pool is being created...");
        mongoClient = new MongoClient(mongoClientURI);

        database = mongoClient.getDatabase("dungeonrealms");
        playerData = database.getCollection("player_data");
        shardData = database.getCollection("shard_data");
        bans = database.getCollection("bans");
        guilds = database.getCollection("guilds");
        quests = database.getCollection("quests");

        Constants.log.info("DungeonRealms Database has connected successfully!");

        createMongoAccessThreads();
    }


    private static void createMongoAccessThreads()
    {
        accessThreads = Lists.newArrayList();

        int count = AsyncUtils.threadCount;
        System.out.println("JVM returns " + count + " processors!");

        // Keep a thread open
        IntStream.range(0, count - 1).forEach(c -> accessThreads.add(new MongoAccessThread()));
        accessThreads.forEach(Thread::start);

        Constants.log.info("DungeonRealms Database mongo access threads ... STARTED ...");
    }

    protected boolean isCacheData()
    {
        return cacheData;
    }
}