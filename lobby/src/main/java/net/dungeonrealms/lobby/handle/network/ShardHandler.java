package net.dungeonrealms.lobby.handle.network;

import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.lobby.ServerLobby;

/**
 * Created by Giovanni on 5-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ShardHandler implements SuperHandler.ListeningHandler
{
    @Override
    public void prepare()
    {
        ServerLobby.getServerLobby().getServer().getPluginManager().registerEvents(this, ServerLobby.getServerLobby());
    }
}
