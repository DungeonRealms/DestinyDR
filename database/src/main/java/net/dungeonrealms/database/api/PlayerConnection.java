package net.dungeonrealms.database.api;

import com.mongodb.client.model.Filters;
import net.dungeonrealms.common.awt.database.mongo.nest.EnumNestType;
import net.dungeonrealms.common.awt.database.mongo.nest.NestDocument;
import net.dungeonrealms.database.Database;
import net.dungeonrealms.database.api.player.DataPlayer;
import net.dungeonrealms.database.lib.DataPipeline;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 10-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PlayerConnection extends DataPipeline {

    private Database database;

    private ConcurrentHashMap<UUID, DataPlayer> dataPlayerCache;

    public PlayerConnection(Database database) {
        this.database = database;
        this.dataPlayerCache = new ConcurrentHashMap<>();
    }

    @Override @Deprecated
    protected void handleConnection(Object object) {
        if (object != null && object instanceof String) {
            String parameter = (String) object;
            UUID uniqueId = UUID.fromString(parameter);
            this.requestData(uniqueId);
        }
    }

    /**
     * Request a player's data & cache it
     *
     * @param uniqueId The unique id of the player
     * @return this
     */
    public PlayerConnection requestData(UUID uniqueId) {
        if (!this.dataPlayerCache.containsKey(uniqueId)) {
            Document document = this.database.getCollection("playerData").find(Filters.eq("genericData.uniqueId", uniqueId.toString())).first();
            // Does the data exist?
            if (document != null && !document.isEmpty()) {
                // Cache the player
                this.dataPlayerCache.put(uniqueId, new DataPlayer(uniqueId, document));
            } else {
                // Send to mongo
                this.database.getCollection("playerData").insertOne(new NestDocument(EnumNestType.PLAYER).generate(uniqueId));
                // Retry
                this.requestData(uniqueId);
            }
        }
        return this;
    }

    /**
     * Remove a player's data from the cache
     * Save before removing {@link #saveData(UUID)}
     *
     * @param uniqueId The unique id of the player
     * @return this
     */
    public PlayerConnection removeData(UUID uniqueId) {
        if (this.dataPlayerCache.containsKey(uniqueId)) {
            this.dataPlayerCache.remove(uniqueId);
        }
        return this;
    }

    /**
     * Save a player's data
     *
     * @param uniqueId The unique id
     * @return this
     */
    public PlayerConnection saveData(UUID uniqueId) {
        if (this.dataPlayerCache.containsKey(uniqueId)) {
            // Instead of bulk writing, we'll use something performance safer.
            DataPlayer dataPlayer = this.dataPlayerCache.get(uniqueId);
            Document document = dataPlayer.constructRawDocument();
            // Insert the raw document of the DataPlayer
            if (!(document != null && document.isEmpty()))
                this.database.getCollection("playerData").updateOne(Filters.eq("genericData.uniqueId", uniqueId.toString()), document);
        }
        return this;
    }

    /**
     * Save all player data
     *
     * @return this
     */
    public PlayerConnection saveAll() {
        this.dataPlayerCache.keySet().forEach(this::saveData);
        return this;
    }

    /**
     * Get the datamodel of a player
     *
     * @param uniqueId The unique id
     * @return The model
     */
    public DataPlayer getIfExists(UUID uniqueId) {
        return this.dataPlayerCache.get(uniqueId);
    }
}
