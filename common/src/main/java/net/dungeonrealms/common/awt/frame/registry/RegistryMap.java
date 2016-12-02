package net.dungeonrealms.common.awt.frame.registry;

import com.google.common.collect.Sets;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class RegistryMap {

    private ConcurrentHashMap<UUID, Registry> registryMap;

    public RegistryMap() {
        this.registryMap = new ConcurrentHashMap<>();
    }

    public Set<Registry> values() {
        // Prevent registry duplicates
        Set<Registry> registrySet = Sets.newHashSet();
        registrySet.addAll(this.registryMap.values());
        return registrySet;
    }

    /**
     * Get a registry by UUID from the map
     *
     * @param key The UUID
     * @return The corresponding registry
     */
    public Registry get(UUID key) {
        return this.registryMap.get(key);
    }

    /**
     * Add a registry to the map
     *
     * @param registry The registry
     */
    public void add(Registry registry) {
        this.registryMap.put(registry.getUniqueId(), registry);
    }

    /**
     * Remove a registry from the map
     *
     * @param registry The registry
     */
    public void remove(Registry registry) {
        if (this.registryMap.containsValue(registry)) {
            this.registryMap.remove(registry.getUniqueId());
        }
    }

    /**
     * Remove a registry from the map by UUID
     *
     * @param uuid The UUID
     */
    public void remove(UUID uuid) {
        this.registryMap.remove(uuid);
    }

    /**
     * Flush the registry map
     */
    public void clear() {
        this.registryMap.clear();
    }

    /**
     * Get the amount of stored registrys
     *
     * @return The count
     */
    public int size() {
        return this.registryMap.size();
    }
}
