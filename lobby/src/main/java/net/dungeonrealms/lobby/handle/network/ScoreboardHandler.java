package net.dungeonrealms.lobby.handle.network;

import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.common.lib.scoreboard.ScoreboardBuilder;
import net.dungeonrealms.common.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.lobby.ServerLobby;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
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
public class ScoreboardHandler implements SuperHandler.ListeningHandler
{
    private int task;

    @Override
    public void prepare()
    {
        ServerLobby.getServerLobby().getServer().getPluginManager().registerEvents(this, ServerLobby.getServerLobby());

        this.task = ServerLobby.getServerLobby().getServer().getScheduler().scheduleAsyncRepeatingTask(ServerLobby.getServerLobby(), () ->
        {
            ScoreboardBuilder scoreboardBuilder = new ScoreboardBuilder(ChatColor.GOLD.toString() + ChatColor.BOLD + " DUNGEON REALMS ");
            scoreboardBuilder.setDisplaySlot(DisplaySlot.SIDEBAR);

            List<BungeeServerInfo> servers = ServerLobby.getServerLobby().getLobbyShard().getShardData();

            if (!servers.isEmpty())
            {
                scoreboardBuilder.setLine(0, ChatColor.YELLOW + "www.dungeonrealms.net");
                scoreboardBuilder.setLine(1, "");
                scoreboardBuilder.setLine(2, ChatColor.YELLOW + "Online shards: " + ChatColor.GREEN + servers.size());
            } else
            {
                scoreboardBuilder.setLine(2, ChatColor.RED + "No shards found!");
            }

            ServerLobby.getServerLobby().getServer().getOnlinePlayers().forEach(scoreboardBuilder::send);
        }, 0L, 20); // Update each second
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        ScoreboardBuilder scoreboardBuilder = new ScoreboardBuilder(ChatColor.GOLD.toString() + ChatColor.BOLD + " DUNGEON REALMS ");
        scoreboardBuilder.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<BungeeServerInfo> servers = ServerLobby.getServerLobby().getLobbyShard().getShardData();

        if (!servers.isEmpty())
        {
            scoreboardBuilder.setLine(0, ChatColor.YELLOW + "www.dungeonrealms.net");
            scoreboardBuilder.setLine(1, "");
            scoreboardBuilder.setLine(2, ChatColor.YELLOW + "Online shards: " + ChatColor.GREEN + servers.size());
        } else
        {
            scoreboardBuilder.setLine(2, ChatColor.RED + "No shards found!");
        }

        scoreboardBuilder.send(event.getPlayer());
    }
}
