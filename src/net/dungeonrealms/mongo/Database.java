package net.dungeonrealms.mongo;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import net.dungeonrealms.mastery.Utils;
import org.bson.Document;

/**
 * Created by Nick on 8/29/2015.
 */
public class Database {

    static Database instance = null;

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public static MongoClient mongoClient = null;
    public static MongoDatabase database = null;
    public static MongoCollection<Document> collection = null;

    public void initConnection() {

        Utils.log.info("Starting Mongo Connection...");

        /*
        Dont worry if it doesn't exist. it will create
        automatically.
         */
        mongoClient = MongoClients.create("mongodb://localhost");
        database = mongoClient.getDatabase("dungeonrealms");
        collection = database.getCollection("player_data");

        Utils.log.info("Mongo has connected successfully!");
    }

}
