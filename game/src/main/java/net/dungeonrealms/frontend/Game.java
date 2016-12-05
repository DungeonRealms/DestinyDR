package net.dungeonrealms.frontend;

import com.google.gson.Gson;
import lombok.Getter;
import net.dungeonrealms.backend.server.CommandRegistry;
import net.dungeonrealms.common.awt.frame.server.shard.GameShard;
import net.dungeonrealms.common.awt.frame.server.shard.ServerCore;
import net.dungeonrealms.common.awt.frame.exception.ServerRunningException;
import net.dungeonrealms.common.awt.frame.save.EnumSaveFlag;
import net.dungeonrealms.backend.server.HandlerRegistry;
import net.dungeonrealms.backend.server.IRegistryRegistry;
import net.dungeonrealms.frontend.vgame.world.GameWorld;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Game extends ServerCore {

    // BOOTSTRAP:
    // -> 1. Create registries
    // -> 2. Register registries/handlers
    // -> 3. Enable registered handlers/registries
    // -> 4. Launch the game

    @Getter
    private static Game game;

    @Getter
    private HandlerRegistry handlerRegistry;

    @Getter
    private IRegistryRegistry registryRegistry;

    @Getter
    private GameWorld gameWorld;

    @Override
    public void onEnable() {
        game = this;

        // 1 Launch all maps (hmap, rmap, cmap)
        this.preEnableMaps();

        // 2 Register the registries (contains all handlers & registries and registers them too)
        this.registerRegistry(registryRegistry = new IRegistryRegistry(this.getRegistryMap()));
        this.registerRegistry(handlerRegistry = new HandlerRegistry(this.getHandlerMap()));
        this.registerRegistry(new CommandRegistry(this.getCommandMap()));

        // 3 Start the shard(server/game)
        try {
            this.enable(new GameShard(this));
        } catch (ServerRunningException e) {
            e.printStackTrace();
        }

        // 4 Load the world generic
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
        // TODO this.disable();
        this.gameWorld.save(EnumSaveFlag.SAVE_QUIT);
    }
}
