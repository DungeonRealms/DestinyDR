package net.dungeonrealms.proxy.handle;

import lombok.Getter;
import net.dungeonrealms.common.awt.BungeeHandler;
import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.proxy.DungeonBungee;
import net.dungeonrealms.proxy.handle.connection.ConnectionHandler;
import net.dungeonrealms.proxy.handle.network.NetworkHandler;
import net.dungeonrealms.proxy.handle.channel.ChannelHandler;
import net.dungeonrealms.proxy.netty.command.CommandAlert;
import net.dungeonrealms.proxy.netty.command.CommandMaintenance;
import net.md_5.bungee.api.ChatColor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 1-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ProxyHandler implements BungeeHandler
{
    @Getter
    protected ConcurrentHashMap<UUID, SuperHandler.Handler> handlerMap;

    @Override
    public void prepare()
    {
        DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.YELLOW + "[ PROXY HANDLER ]");
        DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.GREEN + "Creating atomic reference..");
        this.handlerMap = new ConcurrentHashMap<>();
        DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.GREEN + "Atomic reference created");

        // Provide handlers
        DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.GREEN + "Collecting handlers for atomic reference..");
        this.handlerMap.put(UUID.randomUUID(), new ChannelHandler());
        this.handlerMap.put(UUID.randomUUID(), new ConnectionHandler());
        this.handlerMap.put(UUID.randomUUID(), new NetworkHandler());
        DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.GREEN + "Handlers provided");

        // Register them
        DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.GREEN + "Preparing live handers..");
        handlerMap.values().forEach((handler) -> handler.prepare());
        DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.GREEN + "Live handlers prepared");

        // Register the commands
        DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.GREEN + "Registering commands..");
        DungeonBungee.getDungeonBungee().getProxy().getPluginManager().registerCommand(DungeonBungee.getDungeonBungee(), new CommandMaintenance());
        DungeonBungee.getDungeonBungee().getProxy().getPluginManager().registerCommand(DungeonBungee.getDungeonBungee(), new CommandAlert());
        DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.GREEN + "Commands registered");

        if (this.handlerMap.size() > 0)
            DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.YELLOW + "Handlers prepared: " + this.handlerMap.size());
        else DungeonBungee.getDungeonBungee().getProxy().stop();
    }
}
