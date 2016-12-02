package net.dungeonrealms.common.awt.frame;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.exception.ServerRunningException;
import net.dungeonrealms.common.awt.frame.handler.Handler;
import net.dungeonrealms.common.awt.frame.handler.HandlerMap;
import net.dungeonrealms.common.awt.frame.registry.Registry;
import net.dungeonrealms.common.awt.frame.registry.RegistryMap;
import net.dungeonrealms.common.awt.frame.server.IServer;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Created by Giovanni on 2-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ServerCore extends JavaPlugin implements IServer {

    @Getter
    private HandlerMap handlerMap;

    @Getter
    private RegistryMap registryMap;

    @Getter
    private GameShard gameShard;

    @Getter
    private boolean allowConnections = false;

    @Getter
    protected ServerCore core;

    private ConsoleCommandSender commandSender;

    /**
     * Start the server
     *
     * @param gameShard The game shard
     */
    protected void enable(GameShard gameShard) throws ServerRunningException {
        if (!gameShard.isEnabled()) {
            this.core = this;
            this.commandSender = this.getServer().getConsoleSender();

            this.commandSender.sendMessage(ChatColor.GREEN + "DUNGEONREALMS SERVER IS STARTING");
            this.commandSender.sendMessage(ChatColor.YELLOW + "Developers: VawkeNetty, Evoltr");

            // Enable the registered registries
            this.commandSender.sendMessage(ChatColor.YELLOW + "ENABLING REGISTRIES " + ChatColor.GREEN + "(" + this.registryMap.size() + ")");
            this.registryMap.values().stream().filter(registry -> !registry.isConnected()).forEach(Registry::prepare);
            // Enable the registered handlers
            this.commandSender.sendMessage(ChatColor.YELLOW + "ENABLING HANDLERS " + ChatColor.GREEN + "(" + this.handlerMap.size() + ")");
            this.handlerMap.values().stream().filter(handler -> !handler.isPrepared()).forEach(Handler::prepare);
            // Start the actual shard
            this.commandSender.sendMessage(ChatColor.YELLOW + "STARTING GAME");
            this.gameShard = gameShard;
            this.gameShard.setEnabled(true);
            // Allow player connections
            this.allowConnections = true;
            this.commandSender.sendMessage(ChatColor.GREEN + "INCOMING CONNECTIONS ENABLED");
            // Finished
            this.commandSender.sendMessage(ChatColor.GREEN + "DUNGEONREALMS SERVER - FINISHED");
        } else
            throw new ServerRunningException();
    }

    /**
     * Enable all maps
     */
    protected void preEnableMaps() {
        this.handlerMap = new HandlerMap();
        this.registryMap = new RegistryMap();
    }

    /**
     * Register a handler
     *
     * @param handler The handler to register
     */
    public void registerHandler(Handler handler) {
        this.handlerMap.add(handler);
    }

    /**
     * Stop a handler by UUID
     *
     * @param uuid The UUID of the handler
     */
    public void stopHandler(UUID uuid) {
        this.handlerMap.get(uuid).disable();
        this.handlerMap.remove(uuid);
    }

    /**
     * Stop a handler directly
     *
     * @param handler The handler to stop
     */
    public void stopHandler(Handler handler) {
        this.stopHandler(handler.getUniqueId());
    }

    /**
     * Register a registry
     *
     * @param registry The registry to register
     */
    public void registerRegistry(Registry registry) {
        this.registryMap.add(registry);
    }

    /**
     * Stop a registry by UUID
     *
     * @param uuid The uuid of the registry
     */
    public void stopRegistry(UUID uuid) {
        this.registryMap.get(uuid).disable();
        this.registryMap.remove(uuid);
    }

    /**
     * Stop a handler directly
     *
     * @param registry The registry to stop
     */
    public void stopRegistry(Registry registry) {
        this.stopRegistry(registry.getUniqueId());
    }
}
