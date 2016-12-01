package net.dungeonrealms.vgame;

import com.google.gson.Gson;
import lombok.Getter;
import net.dungeonrealms.backend.GameShard;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.awt.frame.EnumSaveFlag;
import net.dungeonrealms.common.backend.database.connection.exception.ConnectionRunningException;
import net.dungeonrealms.common.backend.database.mongo.connection.MongoConnection;
import net.dungeonrealms.vgame.core.handle.CommandHandler;
import net.dungeonrealms.vgame.core.handle.GameHandler;
import net.dungeonrealms.vgame.core.handle.RegistryHandler;
import net.dungeonrealms.vgame.world.GameWorld;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Game extends JavaPlugin {

    // TODO rewrite this ugly thing

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

    @Getter
    private GameWorld gameWorld;

    @Override
    public void onEnable() {
        game = this;

        //** Logger **//
        this.instanceLogger = this.getServer().getConsoleSender();

        // ** Init the mongo ** //
        this.mongoConnection = new MongoConnection();
        try {
            this.mongoConnection.runOn(Constants.DATABASE_URI, "dungeonrealms");
        } catch (ConnectionRunningException e) {
            // This will never happen
        }
        //** Handlers ** //
        this.handlerCore = new GameHandler();
        this.handlerCore.prepare();

        //** Registries ** //
        this.registryHandler = new RegistryHandler();
        this.registryHandler.prepare();

        // ** Commands ** //
        new CommandHandler().prepare();

        // ** World ** //
        JSONParser jsonParser = new JSONParser();
        try {
            // dataFolder\world\gameWorld.json
            String jsonString = jsonParser.parse(new FileReader(this.getDataFolder() + File.separator + "world" + File.separator + "gameWorld.json")).toString();
            this.gameWorld = new Gson().fromJson(jsonString, GameWorld.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        this.gameWorld.save(EnumSaveFlag.SAVE_QUIT);
    }
}
