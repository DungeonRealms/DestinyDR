package net.dungeonrealms.vgame.player;

import net.dungeonrealms.api.sql.registry.DataRegistry;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Giovanni on 30-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PlayerRegistry implements DataRegistry
{
    @Override
    public void prepare()
    {

    }

    @Override
    public AtomicBoolean atomicPreference()
    {
        return new AtomicBoolean(true);
    }

    @Override
    public ConcurrentHashMap getMap()
    {
        return null;
    }

    @Override
    public void createData()
    {

    }

    @Override
    public void save()
    {

    }

    @Override
    public void collect()
    {

    }
}
