package net.dungeonrealms.vgame;

import lombok.Getter;
import net.dungeonrealms.api.sql.registry.DataRegistry;
import net.dungeonrealms.api.sql.registry.type.WeaponRegistry;
import net.dungeonrealms.common.awt.SuperHandler;
import org.bukkit.ChatColor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class RegistryHandler implements SuperHandler.Handler
{
    @Getter
    private ConcurrentHashMap<UUID, DataRegistry> registryMap;

    @Getter
    private WeaponRegistry weaponRegistry;

    @Override
    public void prepare()
    {
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.YELLOW + "[ HANDLER CORE ]");
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Creating atomic reference..");
        this.registryMap = new ConcurrentHashMap<>();
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Atomic reference created");

        // Provide registries
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Connecting & collecting registries for atomic reference..");
        this.registryMap.put(UUID.randomUUID(), this.weaponRegistry = new WeaponRegistry());
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Handlers connected & collected");

        // Register them
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Preparing connected registries..");
        this.registryMap.values().forEach(dataRegistry -> dataRegistry.prepare());
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Connected registries prepared");

        if (this.registryMap.size() > 0)
            Game.getGame().getInstanceLogger().sendMessage(ChatColor.YELLOW + "Registries prepared: " + this.registryMap.size());
        else Game.getGame().getServer().shutdown();
    }
}
