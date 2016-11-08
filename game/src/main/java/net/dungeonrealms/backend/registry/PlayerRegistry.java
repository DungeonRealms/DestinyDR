package net.dungeonrealms.backend.registry;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.sql.registry.DataRegistry;
import net.dungeonrealms.vgame.player.GamePlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Giovanni on 8-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PlayerRegistry implements DataRegistry
{
    private AtomicReference<ConcurrentHashMap<UUID, GamePlayer>> playerMap;

    @Override
    public void prepare()
    {
        this.playerMap = new AtomicReference<ConcurrentHashMap<UUID, GamePlayer>>();
        this.playerMap.set(new ConcurrentHashMap<>());
    }

    @Override
    public AtomicBoolean atomicPreference()
    {
        return new AtomicBoolean(true);
    }

    @Override
    public ConcurrentHashMap<UUID, GamePlayer> getMap()
    {
        return playerMap.get();
    }

    @Override
    public void collect()
    {
        // Unused
    }

    @Override
    public void save()
    {
        GameAPI.backupDatabase();
    }

    @Override
    public void createData()
    {
        // Unused
    }
}
