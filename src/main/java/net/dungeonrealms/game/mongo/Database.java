package net.dungeonrealms.game.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import net.dungeonrealms.game.guild.db.GuildDatabase;
import net.dungeonrealms.game.mastery.Utils;
import org.bson.Document;

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

    public static com.mongodb.MongoClient mongoClient = null;
    public static MongoClientURI mongoClientURI = null;
    public static com.mongodb.client.MongoDatabase database = null;
    public static com.mongodb.client.MongoCollection<Document> collection = null;
    public static com.mongodb.client.MongoCollection<Document> ranks = null;
    public static com.mongodb.client.MongoCollection<Document> guilds = null;

    public void startInitialization() {
        Utils.log.info("DungeonRealms Starting [MONGODB] Connection...");
        mongoClientURI = new MongoClientURI("mongodb://104.236.116.27:27017/dungeonrealms");
        mongoClient = new MongoClient(mongoClientURI);
        //mongoClient = MongoClients.create("mongodb://104.236.116.27:27017/dungeonrealms");
        database = mongoClient.getDatabase("dungeonrealms");
        collection = database.getCollection("player_data");
        guilds = database.getCollection("guilds");
        ranks = database.getCollection("ranks");

        GuildDatabase.setGuilds(guilds);
        Utils.log.info("DungeonRealms [MONGODB] has connected successfully!");
    }
}
