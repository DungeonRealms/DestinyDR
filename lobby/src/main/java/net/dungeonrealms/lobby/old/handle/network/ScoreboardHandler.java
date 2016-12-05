package net.dungeonrealms.lobby.old.handle.network;

import net.dungeonrealms.common.awt.handler.old.SuperHandler;
import net.dungeonrealms.common.frontend.lib.scoreboard.ScoreboardBuilder;
import net.dungeonrealms.common.old.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.lobby.old.ServerLobby;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.List;

/**
 * Created by Giovanni on 5-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ScoreboardHandler implements SuperHandler.ListeningHandler {
    private int task;

    @Override
    public void prepare() {
        ServerLobby.getServerLobby().getServer().getPluginManager().registerEvents(this, ServerLobby.getServerLobby());

        this.task = ServerLobby.getServerLobby().getServer().getScheduler().scheduleSyncRepeatingTask(ServerLobby.getServerLobby(), () ->
        {
            ScoreboardBuilder scoreboardBuilder = new ScoreboardBuilder(ChatColor.YELLOW.toString() + ChatColor.BOLD + " DUNGEON REALMS ");
            scoreboardBuilder.setDisplaySlot(DisplaySlot.SIDEBAR);

            List<BungeeServerInfo> servers = ServerLobby.getServerLobby().getLobbyShard().getShardData();

            if (!servers.isEmpty() && servers.size() != 1) // We don't want to include the lobby
            {
                scoreboardBuilder.setLine(3, "");
                scoreboardBuilder.setLine(2, ChatColor.GREEN + "Online shards: " + ChatColor.GREEN.toString() + ChatColor.BOLD + servers.size());
                scoreboardBuilder.setLine(1, "");
                scoreboardBuilder.setLine(0, ChatColor.GOLD + "www.dungeonrealms.net");
            } else {
                scoreboardBuilder.setLine(3, "");
                scoreboardBuilder.setLine(2, ChatColor.RED + "No shards found!");
                scoreboardBuilder.setLine(1, "");
                scoreboardBuilder.setLine(0, ChatColor.GOLD + "www.dungeonrealms.net");
            }

            ServerLobby.getServerLobby().getServer().getOnlinePlayers().forEach(scoreboardBuilder::send);
        }, 0L, 20 * 10); // Update each 10 seconds
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ScoreboardBuilder scoreboardBuilder = new ScoreboardBuilder(ChatColor.YELLOW.toString() + ChatColor.BOLD + " DUNGEON REALMS ");
        scoreboardBuilder.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<BungeeServerInfo> servers = ServerLobby.getServerLobby().getLobbyShard().getShardData();

        if (!servers.isEmpty() && servers.size() != 1) // We don't want to include the lobby
        {
            scoreboardBuilder.setLine(3, "");
            scoreboardBuilder.setLine(2, ChatColor.GREEN + "Online shards: " + ChatColor.GREEN.toString() + ChatColor.BOLD + servers.size());
            scoreboardBuilder.setLine(1, "");
            scoreboardBuilder.setLine(0, ChatColor.GOLD + "www.dungeonrealms.net");
        } else {
            scoreboardBuilder.setLine(3, "");
            scoreboardBuilder.setLine(2, ChatColor.RED + "No shards found!");
            scoreboardBuilder.setLine(1, "");
            scoreboardBuilder.setLine(0, ChatColor.GOLD + "www.dungeonrealms.net");
        }

        scoreboardBuilder.send(event.getPlayer());
    }
}
