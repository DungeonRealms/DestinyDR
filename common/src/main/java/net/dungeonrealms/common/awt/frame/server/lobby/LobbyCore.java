package net.dungeonrealms.common.awt.frame.server.lobby;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.command.Command;
import net.dungeonrealms.common.awt.frame.command.CommandMap;
import net.dungeonrealms.common.awt.frame.exception.ServerRunningException;
import net.dungeonrealms.common.awt.frame.handler.Handler;
import net.dungeonrealms.common.awt.frame.handler.HandlerMap;
import net.dungeonrealms.common.awt.frame.registry.Registry;
import net.dungeonrealms.common.awt.frame.server.IServer;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Created by Giovanni on 5-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class LobbyCore extends JavaPlugin implements IServer {

    @Getter
    private JavaPlugin access;

    @Getter
    private HandlerMap handlerMap;

    @Getter
    private CommandMap commandMap;

    @Getter
    private boolean prepared;

    @Getter
    private LobbyServer lobbyServer;

    @Getter
    private transient boolean connectionsAllowed = false;

    private ConsoleCommandSender commandSender;

    /**
     * Start the lobby server
     */
    protected void enable(LobbyServer lobbyServer) throws ServerRunningException {
        if (lobbyServer != null && !lobbyServer.isEnabled()) {
            this.access = this;
            this.commandSender = this.getServer().getConsoleSender();

            this.commandSender.sendMessage(ChatColor.GREEN + "DUNGEONREALMS LOBBY IS STARTING");
            this.commandSender.sendMessage(ChatColor.YELLOW + "Developers: VawkeNetty, Evoltr");

            // Enable the registered handlers
            this.commandSender.sendMessage(ChatColor.YELLOW + "ENABLING SUPER-HANDLERS " + ChatColor.GREEN + "(" + this.handlerMap.size() + ")");
            this.handlerMap.values().stream().filter(handler -> !handler.isPrepared()).forEach(Handler::prepare);
            // Enabled the registered commands
            this.commandSender.sendMessage(ChatColor.YELLOW + "ENABLING COMMANDS " + ChatColor.GREEN + "(" + this.commandMap.size() + ")");
            this.commandMap.values().forEach(Command::register);
            // Start the actual lobby
            this.commandSender.sendMessage(ChatColor.YELLOW + "STARTING LOBBY");
            this.lobbyServer = lobbyServer;
            this.lobbyServer.start();
            this.connectionsAllowed = true;
            this.commandSender.sendMessage(ChatColor.GREEN + "INCOMING CONNECTIONS ENABLED");
            // Finished
            this.commandSender.sendMessage(ChatColor.GREEN + "DUNGEONREALMS LOBBY - FINISHED");
            this.prepared = true;
        } else
            throw new ServerRunningException();
    }

    /**
     * Enable all maps
     */
    protected void preEnableMaps() {
        this.handlerMap = new HandlerMap();
        this.commandMap = new CommandMap();
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
     * Register a command
     *
     * @param command The command to register
     */
    public void registerCommand(Command command) {
        this.commandMap.add(command);
    }

    public void registerEvent(Listener listener) {
        this.getServer().getPluginManager().registerEvents(listener, this);
    }
}
