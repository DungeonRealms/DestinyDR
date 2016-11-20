package net.dungeonrealms.vgame.core.handle;

import net.dungeonrealms.backend.PacketHandler;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.security.NUAIHolder;
import net.dungeonrealms.vgame.security.handle.AtomicHandler;
import net.dungeonrealms.vgame.security.handle.NeutronHandler;
import lombok.Getter;
import net.dungeonrealms.common.awt.handler.SuperHandler;
import org.bukkit.ChatColor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.dungeonrealms.common.awt.handler.SuperHandler.*;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GameHandler implements Handler
{
    @Getter
    protected ConcurrentHashMap<UUID, SuperHandler.Handler> handlerMap;

    @Getter
    private RegistryHandler registryHandler;

    @Override
    public void prepare()
    {
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.YELLOW + "[ GAME HANDLER ]");
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Creating atomic reference..");
        this.handlerMap = new ConcurrentHashMap<>();
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Atomic reference created");

        // Provide handlers
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Collecting handlers for atomic reference..");
        this.handlerMap.put(UUID.randomUUID(), this.registryHandler = new RegistryHandler());
        this.handlerMap.put(UUID.randomUUID(), new PacketHandler());
        this.handlerMap.put(UUID.randomUUID(), new NUAIHolder());
        this.handlerMap.put(UUID.randomUUID(), new AtomicHandler());
        this.handlerMap.put(UUID.randomUUID(), new NeutronHandler());

        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Handlers provided");

        // Register them
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Preparing live handlers..");
        this.handlerMap.values().forEach((handler) -> handler.prepare());
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Live handlers prepared");

        if (this.handlerMap.size() > 0)
            Game.getGame().getInstanceLogger().sendMessage(ChatColor.YELLOW + "Handlers prepared: " + this.handlerMap.size());
        else Game.getGame().getServer().shutdown();
    }
}
