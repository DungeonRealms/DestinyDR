package net.dungeonrealms.vgame;

import net.dungeonrealms.backend.PacketHandler;
import net.dungeonrealms.backend.bungee.BungeeHandler;
import net.dungeonrealms.backend.packet.handle.MonoPacketHandler;
import test.GameTest;
import lombok.Getter;
import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.backend.reboot.RebootHandler;
import net.dungeonrealms.backend.backup.SaveHandler;
import net.dungeonrealms.vgame.item.weapon.handle.BowHandler;
import net.dungeonrealms.vgame.item.weapon.handle.WeaponHandler;
import org.bukkit.ChatColor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.dungeonrealms.common.awt.SuperHandler.*;

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
        this.handlerMap.put(UUID.randomUUID(), new RebootHandler()); // The first handler to ever exist for the recode! yay!
        this.handlerMap.put(UUID.randomUUID(), this.registryHandler = new RegistryHandler());
        this.handlerMap.put(UUID.randomUUID(), new WeaponHandler());
        this.handlerMap.put(UUID.randomUUID(), new BowHandler());
        this.handlerMap.put(UUID.randomUUID(), new SaveHandler());
        this.handlerMap.put(UUID.randomUUID(), new MonoPacketHandler());
        this.handlerMap.put(UUID.randomUUID(), new BungeeHandler());
        this.handlerMap.put(UUID.randomUUID(), new PacketHandler());

        // skelframe
        this.handlerMap.put(UUID.randomUUID(), new GameTest());
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
