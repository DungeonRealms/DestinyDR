package net.dungeonrealms.lobby.handle.chat;

import net.dungeonrealms.common.awt.handler.SuperHandler;
import net.dungeonrealms.common.old.game.database.player.rank.Rank;
import net.dungeonrealms.lobby.ServerLobby;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Created by Giovanni on 5-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ChatHandler implements SuperHandler.ListeningHandler {
    @Override
    public void prepare() {
        ServerLobby.getServerLobby().getServer().getPluginManager().registerEvents(this, ServerLobby.getServerLobby());
    }

    @EventHandler
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        if (!Rank.isDev(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Chat has been disabled in the lobby");
        } else {
            Player player = event.getPlayer();
            event.setFormat(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD + "DEV " + ChatColor.RESET + player.getName() + ": " + ChatColor.GRAY + event.getMessage());
        }
    }
}
