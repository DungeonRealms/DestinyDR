package net.dungeonrealms.backend.server;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.command.Command;
import net.dungeonrealms.common.awt.frame.command.CommandMap;
import net.dungeonrealms.common.awt.frame.registry.Registry;
import net.dungeonrealms.frontend.Game;

import java.util.UUID;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CommandRegistry implements Registry {

    // Handles all commands

    private CommandMap commandMap;

    @Getter
    private UUID uniqueId;

    @Getter
    private boolean connected;

    public CommandRegistry(CommandMap commandMap) {
        this.commandMap = commandMap;
        this.uniqueId = UUID.randomUUID();
    }

    @Override
    public void prepare() {
        // Register all commands

        for (Command command : this.commandMap.values()) {
            Game.getGame().registerCommand(command);
        }
        this.connected = true;
    }

    @Override
    public void disable() {
        this.connected = false;
    }

    @Override
    public boolean ignoreEnabled() {
        return true;
    }
}
