package net.dungeonrealms.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import net.dungeonrealms.database.api.MongoAPI;
import net.dungeonrealms.database.exception.ConnectionRunningException;
import org.bson.Document;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 10-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Database {

    private Mongo mongo;

    @Getter
    private MongoAPI api;

    private boolean running = false;

    public Database(String URI, String fromDatabase) throws ConnectionRunningException {
        if (!running) {
            this.mongo = new Mongo(URI, fromDatabase);
            this.api = new MongoAPI(this);
            this.running = true;
        } else
            throw new ConnectionRunningException();
    }

    /**
     * Close the connection
     */
    public void close() {
        this.mongo.getMongoClient().close();
    }

    /**
     * Get a collection of mongo documents
     *
     * @param identifier The collection identifier
     * @return The collection
     */
    public MongoCollection<Document> getCollection(String identifier) {
        return this.mongo.getCollection(identifier);
    }
//====================================================================================

    /**
     * Created by Giovanni on 10-12-2016.
     * <p>
     * This file is part of the Dungeon Realms project.
     * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
     */
    private class Mongo {

        @Getter
        private MongoClient mongoClient;

        @Getter
        private MongoDatabase mongoDatabase;

        private ConcurrentHashMap<String, MongoCollection<Document>> mongoCollectionHolder;

        public Mongo(String URI, String database) {
            try {
                this.mongoClient = new MongoClient(new MongoClientURI(URI));
                this.mongoDatabase = this.mongoClient.getDatabase(database);

                this.mongoCollectionHolder = new ConcurrentHashMap<>();
                this.mongoCollectionHolder.put("playerData", this.mongoDatabase.getCollection("player_data"));
                this.mongoCollectionHolder.put("banData", this.mongoDatabase.getCollection("bans"));
                this.mongoCollectionHolder.put("guildData", this.mongoDatabase.getCollection("guilds"));
            } catch (Exception e) {
                System.out.println("Failed to setup the Mongo database");
                e.printStackTrace();
            }
        }

        public MongoCollection<Document> getCollection(String identifier) {
            return this.mongoCollectionHolder.get(identifier);
        }
    }
}
