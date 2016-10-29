package net.dungeonrealms.vgame;

import lombok.Getter;
import net.dungeonrealms.awt.SuperHandler;
import net.dungeonrealms.backend.reboot.RebootHandler;
import org.bukkit.ChatColor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.dungeonrealms.awt.SuperHandler.*;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class HandlerCore implements Handler
{
    @Getter
    protected ConcurrentHashMap<UUID, SuperHandler.Handler> handlerMap;

    @Override
    public void prepare()
    {
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "[ HANDLER CORE ]");
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Creating atomic reference..");
        this.handlerMap = new ConcurrentHashMap<>();
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Atomic reference created");

        // Provide handlers
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Collecting handlers for atomic reference..");
        this.handlerMap.put(UUID.randomUUID(), new RebootHandler()); // The first handler to ever exist for the recode! yay!
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Handlers provided");

        // Register them
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Preparing live handers..");
        handlerMap.values().forEach((handler) -> handler.prepare());
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Live handlers prepared");

        if (this.handlerMap.size() > 0)
            Game.getGame().getInstanceLogger().sendMessage(ChatColor.YELLOW + "Handlers prepared: " + this.handlerMap.size());
        else Game.getGame().getServer().shutdown();
    }
}
