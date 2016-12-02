package net.dungeonrealms.common.awt.frame.command;

import lombok.Getter;
import org.bukkit.command.CommandExecutor;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public abstract class Command implements CommandExecutor {

    @Getter
    private String name;

    public Command(String name) {
        this.name = name;
    }

    public void register() {

    }
}
