package net.dungeonrealms.backend.registry;

import net.dungeonrealms.common.old.game.database.sql.registry.DataRegistry;
import net.dungeonrealms.vgame.player.GamePlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Giovanni on 20-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PlayerRegistry implements DataRegistry {
    private ConcurrentHashMap<UUID, GamePlayer> playerMap;

    @Override
    public void prepare() {
        this.playerMap = new ConcurrentHashMap();
    }

    @Override
    public AtomicBoolean atomicPreference() {
        return new AtomicBoolean(false);
    }

    @Override
    public ConcurrentHashMap<UUID, GamePlayer> getMap() {
        return this.playerMap;
    }

    @Override
    public void collect() {
        // Unused
    }

    @Override
    public void createData() {
        // Unused
    }

    @Override
    public void save() {

    }
}
