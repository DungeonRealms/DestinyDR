package net.dungeonrealms.backend.backup;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.awt.handler.SuperHandler;import net.dungeonrealms.vgame.Game;

/**
 * Created by Giovanni on 30-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class SaveHandler implements SuperHandler.Handler
{
    private int saveItemTask;

    private int secondHandle; // In case of a disaster.

    @Override
    public void prepare()
    {
        // Backup database
        Game.getGame().getServer().getScheduler().runTaskTimerAsynchronously(Game.getGame(), GameAPI::backupDatabase, 0L, 12000L);

        this.saveItemTask = Game.getGame().getServer().getScheduler().scheduleAsyncRepeatingTask(Game.getGame(), () -> {
            // Save all data, handled by registries
            Game.getGame().getRegistryHandler().getRegistryMap().values().forEach((dataRegistry) -> dataRegistry.save());
        }, 0L, 20 * Game.getGame().getGameShard().getSaveTime());
    }
}
