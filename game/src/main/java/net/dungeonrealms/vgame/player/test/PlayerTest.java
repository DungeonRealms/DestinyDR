package net.dungeonrealms.vgame.player.test;

import net.dungeonrealms.common.awt.handler.SuperHandler;
import net.dungeonrealms.common.backend.player.DataPlayer;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.player.GamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * Created by Giovanni on 18-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PlayerTest implements SuperHandler.ListeningHandler
{
    @Override
    public void prepare()
    {
        Game.getGame().getServer().getPluginManager().registerEvents(this, Game.getGame());
    }

    @EventHandler
    public void onAsyncLogin(AsyncPlayerPreLoginEvent event)
    {
        // Already done async
        DataPlayer dataPlayer = Game.getGame().getMongoConnection().getApi().requestPlayerData(event.getUniqueId()).getPlayer(event.getUniqueId());
        GamePlayer gamePlayer = new GamePlayer(dataPlayer); // The actual Dungeon Realms game-player

        // -> send it to the registry to allow access
    }
}
