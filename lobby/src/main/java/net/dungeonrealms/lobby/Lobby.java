package net.dungeonrealms.lobby;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Giovanni on 5-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Lobby extends JavaPlugin {

    @Getter
    private static Lobby lobby;

    @Override
    public void onEnable() {
        lobby = this;
    }
}
