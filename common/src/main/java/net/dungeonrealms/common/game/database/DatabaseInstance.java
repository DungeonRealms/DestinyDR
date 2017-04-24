//package net.dungeonrealms.common.game.database;
//
//import com.google.common.collect.Lists;
//import com.mongodb.MongoClient;
//import com.mongodb.MongoClientOptions;
//import com.mongodb.MongoClientURI;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoDatabase;
//
//import net.dungeonrealms.common.Constants;
//import net.dungeonrealms.common.Database;
//import net.dungeonrealms.common.game.database.concurrent.MongoAccessThread;
//import net.dungeonrealms.common.game.util.AsyncUtils;
//
//import org.bson.Document;
//import org.bukkit.Bukkit;
//import org.ini4j.Ini;
//import org.ini4j.InvalidFileFormatException;
//
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.List;
//import java.util.stream.IntStream;
//
///**
// * Created by Nick on 8/29/2015.
// */
//
//public class DatabaseInstance {
//
//    private static DatabaseInstance instance = null;
//
//    public static DatabaseInstance getInstance() {
//        if (instance == null) {
//            instance = new DatabaseInstance();
//        }
//        return instance;
//    }
//
//    public static MongoClient mongoClient = null;
//    public static MongoClientURI mongoClientURI = null;
//    public static MongoDatabase database = null;
//
//    public static List<MongoAccessThread> accessThreads;
//
//    public static MongoCollection<Document> playerData, shardData, bans, guilds, ranks, misc;
//    protected boolean cacheData = true;
//
//    public void startInitialization(boolean cacheData) {
//        this.cacheData = cacheData;
//        try{
//        	Database db = loadFromConfig();
//        	mongoClientURI = new MongoClientURI(db.getURI(), new MongoClientOptions.Builder().maxConnectionIdleTime(0));
//
//        	Constants.log.info("DungeonRealms Database connection pool is being created...");
//        	mongoClient = new MongoClient(mongoClientURI);
//        	database = mongoClient.getDatabase(db.getDatabaseName());
//        	playerData = database.getCollection("player_data");
//        	shardData = database.getCollection("shard_data");
//        	bans = database.getCollection("bans");
//        	guilds = database.getCollection("guilds");
//        	ranks = database.getCollection("ranks");
//        	misc = database.getCollection("misc");
//
//        	Constants.log.info("DungeonRealms Database has connected successfully!");
//
//        	createMongoAccessThreads();
//        } catch (Exception e) {
//        	//Wrong credentials, no credentials in config, config not found, etc.
//        	e.printStackTrace();
//        }
//    }
//
//
//    private static void createMongoAccessThreads() {
//        accessThreads = Lists.newArrayList();
//
//        int count = AsyncUtils.threadCount;
//        System.out.println("JVM returns " + count + " processors!");
//
//        // Keep a thread open
//        IntStream.range(0, count - 1).forEach(c -> accessThreads.add(new MongoAccessThread()));
//        accessThreads.forEach(Thread::start);
//
//
//        Constants.log.info("DungeonRealms Database mongo access threads ... STARTED ...");
//    }
//
//    protected boolean isCacheData() {
//        return cacheData;
//    }
//
//    private static Database loadFromConfig(){
//        Ini ini = new Ini();
//        try {
//            ini.load(new FileReader("credentials.ini"));
//            // Main
//            String database = ini.get("DB", "database", String.class);
//            String username = ini.get("DB", "username", String.class);
//            String password = ini.get("DB", "password", String.class);
//            String hostname = ini.get("DB", "host", String.class);
//            String replicaset = ini.get("DB", "replicaset", String.class);
//            return new Database(hostname, username, password, database, replicaset);
//        } catch (InvalidFileFormatException e1) {
//        	Bukkit.getLogger().info("InvalidFileFormat in credentials.ini!");
//        } catch (FileNotFoundException e1) {
//            Bukkit.getLogger().info("credentials.ini not found");
//        } catch (IOException e1) {
//        	Bukkit.getLogger().info("IOException in credentials.ini!");
//        }
//        return null;
//    }
//}