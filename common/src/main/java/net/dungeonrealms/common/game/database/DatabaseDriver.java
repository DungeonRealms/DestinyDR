package net.dungeonrealms.common.game.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.concurrent.UpdateThread;
import org.bson.Document;

/**
 * Created by Nick on 8/29/2015.
 */

public class DatabaseDriver {

    private static DatabaseDriver instance = null;

    public static DatabaseDriver getInstance() {
        if (instance == null) {
            instance = new DatabaseDriver();
        }
        return instance;
    }

    public static MongoClient mongoClient = null;
    public static MongoClientURI mongoClientURI = null;
    public static MongoDatabase database = null;

    public static MongoCollection<Document> playerData = null;
    public static MongoCollection<Document> bans = null;
    public static MongoCollection<Document> guilds = null;
    public static MongoCollection<Document> quests = null;

    protected boolean cacheData = true;

    public void startInitialization(boolean cacheData) {
        this.cacheData = cacheData;

        Constants.log.info("DungeonRealms Starting [DATABASE] Connection...");
        mongoClientURI = new MongoClientURI(Constants.DATABASE_URI);

        // START UPDATE THREAD //
        new UpdateThread().start();
        Constants.log.info("DungeonRealms - MongoUpdateThread ... STARTED ...");

        mongoClient = new MongoClient(mongoClientURI);
        database = mongoClient.getDatabase("dungeonrealms");
        playerData = database.getCollection("player_data");
        bans = database.getCollection("bans");
        guilds = database.getCollection("guilds");
        quests = database.getCollection("quests");

        Constants.log.info("DungeonRealms [DATABASE] has connected successfully!");
    }

    protected boolean isCacheData() {
        return cacheData;
    }
}