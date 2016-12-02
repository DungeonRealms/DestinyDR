package net.dungeonrealms.vgame;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.GameShard;
import net.dungeonrealms.common.awt.frame.ServerCore;
import net.dungeonrealms.common.awt.frame.exception.ServerRunningException;
import net.dungeonrealms.vgame.server.HandlerRegistry;
import net.dungeonrealms.vgame.server.IRegistryRegistry;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Game extends ServerCore {

    // BOOTSTRAP:
    // -> 1. Create registries
    // -> 2. Register registries/handlers
    // -> 3. Enable registered handlers/registries
    // -> 4. Launch the game

    @Getter
    private static Game game;

    @Getter
    private HandlerRegistry handlerRegistry;

    @Getter
    private IRegistryRegistry registryRegistry;

    @Override
    public void onEnable() {
        game = this;

        // 1 Launch all maps (hmap, rmap..)
        this.preEnableMaps();

        // 2 Register the registries (contains all handlers & registries and registers them too)
        this.registerRegistry(registryRegistry = new IRegistryRegistry(this.getRegistryMap()));
        this.registerRegistry(handlerRegistry = new HandlerRegistry(this.getHandlerMap()));

        // 3 Start the shard(server/game)
        try {
            this.enable(new GameShard(this));
        } catch (ServerRunningException e) {
            e.printStackTrace();
        }
    }
}
