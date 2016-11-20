package net.dungeonrealms.common.backend.player.data.type;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.backend.player.data.IData;
import net.dungeonrealms.common.backend.player.data.enumeration.EnumKeyShardTier;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 19-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CollectionData implements IData
{
    @Getter
    private UUID owner;

    public CollectionData(UUID uuid, Document keyShards, Document collectibles)
    {
        this.owner = uuid;
        this.keyShardMap = new ConcurrentHashMap<>();
        this.keyShardMap.put(EnumKeyShardTier.T1, keyShards.getInteger("tier1"));
        this.keyShardMap.put(EnumKeyShardTier.T2, keyShards.getInteger("tier2"));
        this.keyShardMap.put(EnumKeyShardTier.T3, keyShards.getInteger("tier3"));
        this.keyShardMap.put(EnumKeyShardTier.T4, keyShards.getInteger("tier4"));
        this.keyShardMap.put(EnumKeyShardTier.T5, keyShards.getInteger("tier5"));
        this.achievements = collectibles.get("achievements", ArrayList.class);
    }

    @Getter
    @Setter
    private List<String> achievements;

    @Getter
    private ConcurrentHashMap<EnumKeyShardTier, Integer> keyShardMap;

    public int getKeyShards(EnumKeyShardTier keyShardTier)
    {
        return this.keyShardMap.get(keyShardTier);
    }
}
