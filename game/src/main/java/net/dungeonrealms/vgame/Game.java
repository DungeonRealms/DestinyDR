package net.dungeonrealms.vgame;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Game extends JavaPlugin
{
    @Getter
    private static Game game;

    @Getter
    private HandlerCore handlerCore;

    @Override
    public void onEnable()
    {
        game = this;

        //** Handlers **//
        this.handlerCore = new HandlerCore();
        this.handlerCore.prepare();
    }
}
