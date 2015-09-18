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
        Utils.log.info("DungeonRealms Starting [MONGODB] Connection...");
        //mongoClient = MongoClients.create("mongodb://admin:admin123@ds051970.mongolab.com:51970/dungeonrealms");
        mongoClient = MongoClients.create("mongodb://druser:drpass@ds051970.mongolab.com:51970/dungeonrealms");
        database = mongoClient.getDatabase("dungeonrealms");
        collection = database.getCollection("player_data");
        Utils.log.info("DungeonRealms [MONGODB] has connected successfully!");
    }

}
