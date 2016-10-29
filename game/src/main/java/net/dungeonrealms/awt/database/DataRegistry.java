package net.dungeonrealms.awt.database;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface DataRegistry
{
    AtomicBoolean atomicPreference();

    // Always use maps with atomic references
    ConcurrentHashMap getMap();
}
