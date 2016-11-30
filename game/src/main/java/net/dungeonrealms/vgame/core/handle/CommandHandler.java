package net.dungeonrealms.vgame.core.handle;

import net.dungeonrealms.common.awt.handler.SuperHandler;
import net.dungeonrealms.common.frontend.command.CommandManager;
import net.dungeonrealms.vgame.Game;
import org.bukkit.ChatColor;

/**
 * Created by Giovanni on 13-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CommandHandler implements SuperHandler.Handler {
    @Override
    public void prepare() {
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.YELLOW + "[ COMMAND HANDLER ]");

        // Register commands
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Loading commands..");
        CommandManager commandManager = new CommandManager();
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Commands registered, continue");
    }
}
