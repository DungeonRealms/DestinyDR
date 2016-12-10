package net.dungeonrealms.common.awt.database.mongo;

import com.mongodb.client.model.Filters;
import lombok.Getter;
import net.dungeonrealms.common.awt.data.DataPlayer;
import net.dungeonrealms.common.awt.database.mongo.nest.EnumNestType;
import net.dungeonrealms.common.awt.database.mongo.nest.NestDocument;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 18-11-2016.
 * <p>
 * All API methods must be handled async/hyper-threaded
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class MongoAPI {

    @Getter
    private Mongo mongo;

    @Getter
    private ConcurrentHashMap<UUID, DataPlayer> dataPlayerMap;

    public MongoAPI(Mongo mongo) {
        this.mongo = mongo;
        this.dataPlayerMap = new ConcurrentHashMap<>();
    }

    /**
     * Requests player data by UUID & caches the player
     *
     * @param uniqueId The unique id of the player
     * @return this
     */
    public MongoAPI requestPlayerData(UUID uniqueId) {
        Document document = this.mongo.getCollection("playerData").find(Filters.eq("genericData.uniqueId", uniqueId.toString())).first();
        // Does the data exist?
        if (document != null && !document.isEmpty()) {
            // Cache the player
            this.dataPlayerMap.put(uniqueId, new DataPlayer(uniqueId, document));
        } else {
            // Send to mongo
            this.mongo.getCollection("playerData").insertOne(new NestDocument(EnumNestType.PLAYER).generate(uniqueId));
            // Retry
            this.requestPlayerData(uniqueId);
        }
        return this;
    }

    /**
     * Remove a player's data from the mongo net.dungeonrealms.database.connection
     *
     * @param uniqueId The unique id of the player
     * @param remove   Boolean
     * @return this
     */
    public MongoAPI saveDataPlayer(UUID uniqueId, boolean remove) {
        if (this.dataPlayerMap.containsKey(uniqueId)) {
            // Instead of bulk writing, we'll use something performance safer.
            DataPlayer dataPlayer = this.dataPlayerMap.get(uniqueId);
            Document document = dataPlayer.constructRawDocument();
            // Insert the raw document of the DataPlayer
            if (!(document != null && document.isEmpty()))
                this.mongo.getCollection("playerData").updateOne(Filters.eq("genericData.uniqueId", uniqueId.toString()), document);
            else
                System.out.println("Failed to remove the player data of: " + uniqueId + " > Document is empty/non-existent");
            if (remove) {
                this.dataPlayerMap.remove(uniqueId);
            }
        }
        return this;
    }

    /**
     * This must be called after the player has been requested
     * using #requestPlayerData(parameter)
     *
     * @param uniqueId The unique id of the player
     * @return the dataplayer
     */
    public DataPlayer getPlayer(UUID uniqueId) {
        return this.dataPlayerMap.get(uniqueId);
    }

    /**
     * Check if a player's data is cached
     *
     * @param uniqueId The player's uuid
     * @return Boolean
     */
    public boolean exists(UUID uniqueId) {
        return this.dataPlayerMap.containsKey(uniqueId);
    }
}
