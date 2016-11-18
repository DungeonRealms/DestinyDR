package net.dungeonrealms.common.backend.database.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.bson.Document;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 18-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Mongo
{

    // new Mongo(uri, database);

    @Getter
    private MongoClient mongoClient;

    @Getter
    private MongoDatabase mongoDatabase;

    private ConcurrentHashMap<String, MongoCollection<Document>> mongoCollectionHolder;

    public Mongo(String URI, String database)
    {
        try
        {
            this.mongoClient = new MongoClient(new MongoClientURI(URI));
            this.mongoDatabase = this.mongoClient.getDatabase(database);

            this.mongoCollectionHolder = new ConcurrentHashMap<>();
            this.mongoCollectionHolder.put("playerData", this.mongoDatabase.getCollection("player_data"));
            this.mongoCollectionHolder.put("shardData", this.mongoDatabase.getCollection("shard_data"));
            this.mongoCollectionHolder.put("banData", this.mongoDatabase.getCollection("bans"));
            this.mongoCollectionHolder.put("guildData", this.mongoDatabase.getCollection("guilds"));
        } catch (Exception e)
        {
            System.out.println("Failed to setup the Mongo database");
            e.printStackTrace();
        }
    }

    public MongoCollection<Document> getCollection(String identifier)
    {
        return this.mongoCollectionHolder.get(identifier);
    }
}
