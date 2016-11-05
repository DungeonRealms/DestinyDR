package net.dungeonrealms.lobby;

import lombok.Getter;
import net.dungeonrealms.common.game.database.DatabaseInstance;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.lobby.handle.LobbyHandler;
import net.dungeonrealms.lobby.ol.effect.GhostFactory;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Created by Giovanni on 5-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ServerLobby extends JavaPlugin
{
    @Getter
    private static ServerLobby serverLobby;

    @Getter
    private ConsoleCommandSender instanceLogger;

    @Getter
    private GhostFactory ghostFactory;

    @Getter
    private LobbyShard lobbyShard;

    @Override
    public void onEnable()
    {
        serverLobby = this;

        // Backend
        BungeeUtils.setPlugin(this);
        BungeeServerTracker.startTask(3L);
        DatabaseInstance.getInstance().startInitialization(true);

        this.instanceLogger = this.getServer().getConsoleSender();

        this.ghostFactory = new GhostFactory(this);

        new LobbyHandler().prepare();

        this.lobbyShard = new LobbyShard(UUID.randomUUID());
    }
}
