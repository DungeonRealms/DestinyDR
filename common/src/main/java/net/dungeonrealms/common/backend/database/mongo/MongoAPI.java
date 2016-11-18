package net.dungeonrealms.common.backend.database.mongo;

import com.mongodb.client.model.Filters;
import lombok.Getter;
import net.dungeonrealms.common.backend.player.DataPlayer;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 18-11-2016.
 *
 * All API methods must be handled async/hyper-threaded
 *
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class MongoAPI
{
    @Getter
    private Mongo mongo;

    @Getter
    private ConcurrentHashMap<UUID, DataPlayer> dataPlayerMap;

    public MongoAPI(Mongo mongo)
    {
        this.mongo = mongo;
        this.dataPlayerMap = new ConcurrentHashMap<>();
    }

    /**
     * Requests player data by UUID & caches the player
     *
     * @param uniqueId
     * @return this
     */
    public MongoAPI requestPlayerData(UUID uniqueId)
    {
        Document document = this.mongo.getCollection("playerData").find(Filters.eq("pinfo.uniqueId", uniqueId.toString())).first();
        if (document != null && !document.isEmpty())
        {
            this.dataPlayerMap.put(uniqueId, null);
        }
        return this;
    }

    /**
     * Removes a dataplayer from the dataPlayerMap & updates the document
     *
     * @param uniqueId
     * @return this
     */
    public MongoAPI removePlayerData(UUID uniqueId)
    {
        if (this.dataPlayerMap.containsKey(uniqueId))
        {
            this.dataPlayerMap.remove(uniqueId);
        }
        return this;
    }

    /**
     * This must be called after the player has been requested
     * using #requestPlayerData(parameter)
     *
     * @param uniqueId
     * @return the dataplayer
     */
    public DataPlayer getPlayer(UUID uniqueId)
    {
        return this.dataPlayerMap.get(uniqueId);
    }
}
