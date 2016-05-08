package net.dungeonrealms.game.mongo;

import org.bson.Document;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;

import net.dungeonrealms.game.mastery.Utils;

/**
 * Created by Nick on 8/29/2015.
 */
public class Database {

    private static Database instance = null;

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public static MongoClient mongoClient = null;
    public static MongoDatabase database = null;
    public static MongoCollection<Document> collection = null;
    public static MongoCollection<Document> ranks = null;
    public static MongoCollection<Document> guilds = null;

    public void startInitialization() {
        Utils.log.info("DungeonRealms Starting [MONGODB] Connection...");
        mongoClient = MongoClients.create("mongodb://104.236.116.27:27017/dungeonrealms");
        database = mongoClient.getDatabase("dungeonrealms");
        collection = database.getCollection("player_data");
        guilds = database.getCollection("guilds");
        ranks = database.getCollection("ranks");
        Utils.log.info("DungeonRealms [MONGODB] has connected successfully!");
    }

}
