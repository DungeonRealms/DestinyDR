package net.dungeonrealms.backend.registry;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.registry.Registry;
import net.dungeonrealms.frontend.vgame.player.GamePlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PlayerRegistry implements Registry {

    @Getter
    private UUID uniqueId;

    @Getter
    private boolean connected;

    private final AtomicReference<ConcurrentHashMap<UUID, GamePlayer>> onlinePlayers = new AtomicReference<>();

    public PlayerRegistry() {
        this.uniqueId = UUID.randomUUID();
    }

    @Override
    public void prepare() {
        this.onlinePlayers.set(new ConcurrentHashMap<>());
        this.connected = true;
    }

    @Override
    public void disable() {
        for (GamePlayer gamePlayer : this.onlinePlayers.get().values()) {
            // TODO store data
        }
    }

    @Override
    public boolean ignoreEnabled() {
        return false;
    }

    public ConcurrentHashMap<UUID, GamePlayer> getOnlinePlayers() {
        return this.onlinePlayers.get();
    }

    public void acceptConnection(GamePlayer gamePlayer) {
        this.onlinePlayers.get().put(gamePlayer.getData().getUniqueId(), gamePlayer);
    }

    public GamePlayer getPlayer(UUID uuid) {
        return this.onlinePlayers.get().get(uuid);
    }
}
