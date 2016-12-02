package net.dungeonrealms.common.awt.frame.handler;

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
public class HandlerMap {

    private ConcurrentHashMap<UUID, Handler> handlerMap;

    public HandlerMap() {
        this.handlerMap = new ConcurrentHashMap<>();
    }

    public Set<Handler> values() {
        // Prevent handler duplicates
        Set<Handler> handlerSet = Sets.newHashSet();
        handlerSet.addAll(this.handlerMap.values());
        return handlerSet;
    }

    /**
     * Get a handler by UUID from the map
     *
     * @param key The UUID
     * @return The corresponding handler
     */
    public Handler get(UUID key) {
        return this.handlerMap.get(key);
    }

    /**
     * Add a handler to the map
     *
     * @param handler The handler
     */
    public void add(Handler handler) {
        this.handlerMap.put(handler.getUniqueId(), handler);
    }

    /**
     * Remove a handler from the map
     *
     * @param handler The handler
     */
    public void remove(Handler handler) {
        if (this.handlerMap.containsValue(handler)) {
            this.handlerMap.remove(handler.getUniqueId());
        }
    }

    /**
     * Remove a handler from the map by UUID
     *
     * @param uuid The UUID
     */
    public void remove(UUID uuid) {
        this.handlerMap.remove(uuid);
    }

    /**
     * Flush the handler map
     */
    public void flush() {
        this.handlerMap.clear();
    }

    /**
     * Get the amount of stored handlers
     *
     * @return The count
     */
    public int size() {
        return this.handlerMap.size();
    }
}
