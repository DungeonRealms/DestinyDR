package net.dungeonrealms.backend.server;

import lombok.Getter;
import net.dungeonrealms.backend.registry.PlayerRegistry;
import net.dungeonrealms.common.awt.frame.registry.Registry;
import net.dungeonrealms.common.awt.frame.registry.RegistryMap;
import net.dungeonrealms.frontend.Game;

import java.util.UUID;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class IRegistryRegistry implements Registry {

    // Handles all registries

    private RegistryMap registryMap;

    @Getter
    private UUID uniqueId;

    @Getter
    private boolean connected;

    // Standalone registries, these must also be stored in the map
    @Getter
    private PlayerRegistry playerRegistry;

    public IRegistryRegistry(RegistryMap registryMap) {
        this.registryMap = registryMap;
        this.uniqueId = UUID.randomUUID();
    }

    @Override
    public void prepare() {
        this.registryMap.add(playerRegistry = new PlayerRegistry());
        this.connected = true;
    }

    @Override
    public void disable() {
        // Disable all handlers
        this.registryMap.values().stream().filter(Registry::isConnected).forEach(registry -> {
            Game.getGame().stopRegistry(registry);
        });
        this.connected = false;
    }

    @Override
    public boolean ignoreEnabled() {
        return true;
    }
}
