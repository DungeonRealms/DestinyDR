package net.dungeonrealms.updated.connection;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.updated.Handler;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by Giovanni on 23-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ConnectionHandler implements Handler, Listener {

    @Override
    public void prepare() {
        DungeonRealms.getInstance().getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    @EventHandler
    public void onAsyncLogin(AsyncPlayerPreLoginEvent event) {
        DatabaseAPI.getInstance().requestPlayer(event.getUniqueId(), false);
        if ((Boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, event.getUniqueId())) {
            String shard = DatabaseAPI.getInstance().getFormattedShardName(event.getUniqueId());
            if (!shard.equals("") && !DungeonRealms.getInstance().shardid.equals(shard)) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.YELLOW.toString() + "The account " + ChatColor.BOLD.toString() + event.getName() + ChatColor.YELLOW.toString()

                        + " is already logged in on " + ChatColor.UNDERLINE.toString() + shard + "." + "\n\n" + ChatColor.GRAY.toString()
                        + "If you have just recently changed servers, your character data is being synced -- " + ChatColor.UNDERLINE.toString()
                        + "wait a few seconds" + ChatColor.GRAY.toString() + " before reconnecting.");
                return;
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        DungeonRealms.getInstance().getPlayerJoinPipeline().handle(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        DungeonRealms.getInstance().getPlayerQuitPipeline().handle(event.getPlayer().getUniqueId());
    }
}
