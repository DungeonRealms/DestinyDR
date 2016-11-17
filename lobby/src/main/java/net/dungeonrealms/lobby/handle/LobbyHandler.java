package net.dungeonrealms.lobby.handle;

import lombok.Getter;
import net.dungeonrealms.common.awt.handler.SuperHandler;
import net.dungeonrealms.lobby.ServerLobby;
import net.dungeonrealms.lobby.handle.chat.ChatHandler;
import net.dungeonrealms.lobby.handle.connection.ConnectionHandler;
import net.dungeonrealms.lobby.handle.generic.GenericHandler;
import net.dungeonrealms.lobby.handle.network.ScoreboardHandler;
import net.dungeonrealms.lobby.handle.network.ShardHandler;
import net.md_5.bungee.api.ChatColor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 5-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class LobbyHandler implements SuperHandler.Handler
{
    @Getter
    protected ConcurrentHashMap<UUID, SuperHandler.Handler> handlerMap;

    @Override
    public void prepare()
    {
        ServerLobby.getServerLobby().getInstanceLogger().sendMessage(ChatColor.YELLOW + "[ LOBBY HANDLER ]");
        ServerLobby.getServerLobby().getInstanceLogger().sendMessage(ChatColor.GREEN + "Creating atomic reference..");
        this.handlerMap = new ConcurrentHashMap<>();
        ServerLobby.getServerLobby().getInstanceLogger().sendMessage(ChatColor.GREEN + "Atomic reference created");

        // Provide handlers
        ServerLobby.getServerLobby().getInstanceLogger().sendMessage(ChatColor.GREEN + "Collecting handlers for atomic reference..");
        this.handlerMap.put(UUID.randomUUID(), new ConnectionHandler());
        this.handlerMap.put(UUID.randomUUID(), new GenericHandler());
        this.handlerMap.put(UUID.randomUUID(), new ChatHandler());
        this.handlerMap.put(UUID.randomUUID(), new ScoreboardHandler());
        this.handlerMap.put(UUID.randomUUID(), new ShardHandler());
        ServerLobby.getServerLobby().getInstanceLogger().sendMessage(ChatColor.GREEN + "Handlers provided");

        // Register them
        ServerLobby.getServerLobby().getInstanceLogger().sendMessage(ChatColor.GREEN + "Preparing live handlers..");
        this.handlerMap.values().forEach(SuperHandler.Handler::prepare);
        ServerLobby.getServerLobby().getInstanceLogger().sendMessage(ChatColor.GREEN + "Live handlers prepared");

        // Register the commands
        ServerLobby.getServerLobby().getInstanceLogger().sendMessage(ChatColor.GREEN + "Registering commands..");
        ServerLobby.getServerLobby().getInstanceLogger().sendMessage(ChatColor.GREEN + "Commands registered");

        if (this.handlerMap.size() > 0)
            ServerLobby.getServerLobby().getInstanceLogger().sendMessage(ChatColor.YELLOW + "Handlers prepared: " + this.handlerMap.size());
        else ServerLobby.getServerLobby().getServer().shutdown();
    }
}
