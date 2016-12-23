package net.dungeonrealms.common.game.database.async;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Giovanni on 23-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface AsynchronousService {

    default void goAsync(JavaPlugin javaPlugin, Runnable runnable) {
        javaPlugin.getServer().getScheduler().scheduleAsyncDelayedTask(javaPlugin, runnable);
    }
}
