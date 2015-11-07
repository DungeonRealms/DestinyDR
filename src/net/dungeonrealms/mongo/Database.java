package net.dungeonrealms.mongo;

import org.bson.Document;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;

import net.dungeonrealms.mastery.Utils;

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
        //mongoClient = MongoClients.create("mongodb://druser:drpass@ds051970.mongolab.com:51970/dungeonrealms");
        mongoClient = MongoClients.create("mongodb://dungeonuser:mwH47e552qxWPwxL@ds051874-a0.mongolab.com:51874,ds051874-a1.mongolab.com:51874/dungeonrealms?replicaSet=rs-ds051874");
        database = mongoClient.getDatabase("dungeonrealms");
        collection = database.getCollection("player_data");
        guilds = database.getCollection("guilds");
        ranks = database.getCollection("ranks");
        Utils.log.info("DungeonRealms [MONGODB] has connected successfully!");
    }

}
