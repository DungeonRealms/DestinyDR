package net.dungeonrealms.common.game.database.async;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Giovanni on 23-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public abstract class AsyncMongoFunction {

    private JavaPlugin instance;

    private int function;

    public AsyncMongoFunction(JavaPlugin instance) {
        this.instance = instance;
    }

    public abstract void function();

    /**
     * Execute the asynchronous database task
     *
     * @param parX Time to execute after
     */
    public void execute(int parX) {
        if (this.instance.isEnabled()) {
            this.function = this.instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> {
                this.function();
            }, 20 * parX);
        }
    }
}
