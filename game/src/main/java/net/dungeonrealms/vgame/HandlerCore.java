package net.dungeonrealms.vgame;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.awt.SuperHandler;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class HandlerCore implements SuperHandler.Handler {
    private ConcurrentHashMap<UUID, SuperHandler.Handler> handlerMap;

    @Override
    public void prepare() {
        this.handlerMap = new ConcurrentHashMap<>();

        handlerMap.values().forEach(handler -> {
            handler.prepare();
            if (handler instanceof SuperHandler.ListeningHandler) {
                SuperHandler.ListeningHandler listeningHandler = (SuperHandler.ListeningHandler) handler;
                DungeonRealms.getInstance().getServer().getPluginManager().registerEvents(listeningHandler, DungeonRealms.getInstance());
            }
        });
    }
}
