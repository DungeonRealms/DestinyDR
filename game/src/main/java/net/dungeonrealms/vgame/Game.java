package net.dungeonrealms.vgame;

import io.vawke.skelframe.SkelRuntime;
import lombok.Getter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.backend.GameShard;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.backend.database.connection.exception.ConnectionRunningException;
import net.dungeonrealms.common.backend.database.mongo.connection.MongoConnection;
import net.dungeonrealms.common.old.game.util.AsyncUtils;
import net.dungeonrealms.vgame.handle.CommandHandler;
import net.dungeonrealms.vgame.handle.GameHandler;
import net.dungeonrealms.vgame.handle.RegistryHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.Executors;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Game extends JavaPlugin
{
    /**
     * TODO keep in mind to register the realms mechanic.
     * if (realmnumber >= 0) mm.registerMechanic(Realms.getInstance());
     * pseudo
     */
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

    @Getter
    private MongoConnection mongoConnection;

    @Override
    public void onEnable()
    {
        game = this;

        //** Logger **//
        this.instanceLogger = this.getServer().getConsoleSender();

        // ** Init the mongo ** //
        this.mongoConnection = new MongoConnection();
        try
        {
            this.mongoConnection.runOn(Constants.DATABASE_URI, "dungeonrealms");
        } catch (ConnectionRunningException e)
        {
            e.printStackTrace(); // This will never happen
        }

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

        // ** Commands **//
        new CommandHandler().prepare();
    }

    @Override
    public void onDisable()
    {
        GameAPI.stopGame();
    }
}
