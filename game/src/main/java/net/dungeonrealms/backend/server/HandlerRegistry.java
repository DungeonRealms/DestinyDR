package net.dungeonrealms.backend.server;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.handler.Handler;
import net.dungeonrealms.common.awt.frame.handler.HandlerMap;
import net.dungeonrealms.common.awt.frame.registry.Registry;
import net.dungeonrealms.frontend.Game;
import net.dungeonrealms.frontend.vgame.item.security.handle.AtomicHandler;
import net.dungeonrealms.frontend.vgame.item.security.handle.NeutronHandler;
import net.dungeonrealms.frontend.vgame.player.connect.PlayerConnection;
import net.dungeonrealms.frontend.vgame.player.goal.achievement.handle.AchievementHandler;
import net.dungeonrealms.frontend.vgame.player.teleportation.TeleportationHandler;
import net.dungeonrealms.frontend.vgame.world.entity.generic.handle.GenericEntityHandler;

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
        // 1 Items
        this.handlerMap.add(new NeutronHandler());
        this.handlerMap.add(new AtomicHandler());
        // 2 Player
        this.handlerMap.add(new AchievementHandler());
        this.handlerMap.add(new TeleportationHandler());
        this.handlerMap.add(new PlayerConnection());
        // 3 Entity
        this.handlerMap.add(new GenericEntityHandler());

        for (Handler handler : this.handlerMap.values()) {
            Game.getGame().registerHandler(handler);
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
