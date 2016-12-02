package net.dungeonrealms.common.awt.frame.command;

import com.google.common.collect.Sets;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CommandMap {
    
    private ConcurrentHashMap<String, Command> commandMap;

    public CommandMap() {
        this.commandMap = new ConcurrentHashMap<>();
    }

    public Set<Command> values() {
        // Prevent command duplicates
        Set<Command> commandSet = Sets.newHashSet();
        commandSet.addAll(this.commandMap.values());
        return commandSet;
    }

    /**
     * Get a command by name from the map
     *
     * @param key The name
     * @return The corresponding command
     */
    public Command get(String key) {
        return this.commandMap.get(key);
    }

    /**
     * Add a command to the map
     *
     * @param command The command
     */
    public void add(Command command) {
        this.commandMap.put(command.getName(), command);
    }

    /**
     * Remove a command from the map
     *
     * @param command The command
     */
    public void remove(Command command) {
        if (this.commandMap.containsValue(command)) {
            this.commandMap.remove(command.getName());
        }
    }

    /**
     * Remove a command from the map by name
     *
     * @param name The name
     */
    public void remove(String name) {
        this.commandMap.remove(name);
    }

    /**
     * Flush the command map
     */
    public void flush() {
        this.commandMap.clear();
    }

    /**
     * Get the amount of stored commands
     *
     * @return The count
     */
    public int size() {
        return this.commandMap.size();
    }
}
