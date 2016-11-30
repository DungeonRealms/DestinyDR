package net.dungeonrealms.common.old.game.database.sql.registry;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface DataRegistry {
    void prepare();

    void save();

    void collect();

    AtomicBoolean atomicPreference();

    ConcurrentHashMap getMap();

    void createData();
}
