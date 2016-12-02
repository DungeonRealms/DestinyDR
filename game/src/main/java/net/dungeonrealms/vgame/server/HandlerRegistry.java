package net.dungeonrealms.vgame.server;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.handler.Handler;
import net.dungeonrealms.common.awt.frame.handler.HandlerMap;
import net.dungeonrealms.common.awt.frame.registry.Registry;
import net.dungeonrealms.vgame.Game;

import java.util.UUID;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class HandlerRegistry implements Registry {

    // Handles all handlers

    private HandlerMap handlerMap;

    @Getter
    private UUID uniqueId;

    @Getter
    private boolean connected;

    public HandlerRegistry(HandlerMap handlerMap) {
        this.handlerMap = handlerMap;
        this.uniqueId = UUID.randomUUID();
    }

    @Override
    public void prepare() {
        // Register all handlers
        for (Handler handler : this.handlerMap.values()) {
            net.dungeonrealms.vgame.old.Game.getGame().registerHandler(handler);
        }
        this.connected = true;
    }

    @Override
    public void disable() {
        // Disable all handlers
        this.handlerMap.values().stream().filter(Handler::isPrepared).forEach(handler -> {
            Game.getGame().stopHandler(handler);
        });
        this.connected = false;
    }

    @Override
    public boolean ignoreEnabled() {
        return true;
    }
}
