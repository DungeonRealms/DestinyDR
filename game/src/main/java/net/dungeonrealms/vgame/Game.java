package net.dungeonrealms.vgame;

import lombok.Getter;
import net.dungeonrealms.backend.GameShard;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.io.FileReader;

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
    private GameHandler handlerCore;

    @Getter
    private RegistryHandler registryHandler;

    @Getter
    private ConsoleCommandSender instanceLogger;

    @Getter
    private GameShard gameShard;

    @Override
    public void onEnable()
    {
        game = this;

        //** Logger **//
        this.instanceLogger = this.getServer().getConsoleSender();
        // ** Init shard **//
        try
        {
            this.gameShard = new GameShard(new FileReader("shardConfig.ini"));
        } catch (FileNotFoundException e)
        {
            this.instanceLogger.sendMessage(ChatColor.RED + "ShardConfiguration not found, shutting down..");
            Game.getGame().getServer().shutdown();
        }
        //** Handlers **//
        this.handlerCore = new GameHandler();
        this.handlerCore.prepare();

        //** Registries **//
        this.registryHandler = new RegistryHandler();
        this.registryHandler.prepare();
    }
}
