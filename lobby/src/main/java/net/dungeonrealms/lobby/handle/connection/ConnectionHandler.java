package net.dungeonrealms.lobby.handle.connection;

import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.common.old.game.database.DatabaseAPI;
import net.dungeonrealms.common.old.game.database.player.rank.Rank;
import net.dungeonrealms.common.old.game.punishment.PunishAPI;
import net.dungeonrealms.common.frontend.lib.message.CenteredMessage;
import net.dungeonrealms.lobby.ServerLobby;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Created by Giovanni on 5-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ConnectionHandler implements SuperHandler.ListeningHandler
{
    @Override
    public void prepare()
    {
        ServerLobby.getServerLobby().getServer().getPluginManager().registerEvents(this, ServerLobby.getServerLobby());
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event)
    {
        if (!PunishAPI.getInstance().isBanned(event.getUniqueId()))
        {
            DatabaseAPI.getInstance().requestPlayer(event.getUniqueId(), false);
        } else
        {
            String bannedMessage = PunishAPI.getInstance().getBannedMessage(event.getUniqueId());
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
            event.setKickMessage(bannedMessage);

            if (DatabaseAPI.getInstance().PLAYERS.containsKey(event.getUniqueId()))
            {
                DatabaseAPI.getInstance().PLAYERS.remove(event.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(ServerLobby.getServerLobby(), () -> {

            player.setPlayerListName(Rank.colorFromRank(Rank.getInstance().getRank(player.getUniqueId())) + player.getName());
            player.setDisplayName(Rank.colorFromRank(Rank.getInstance().getRank(player.getUniqueId())) + player.getName());
            player.setCustomName(Rank.colorFromRank(Rank.getInstance().getRank(player.getUniqueId())) + player.getName());

            player.teleport(new Location(player.getWorld(), -972.5, 13.5, -275.5));

            ServerLobby.getServerLobby().getGhostFactory().addPlayer(player);
            ServerLobby.getServerLobby().getGhostFactory().setGhost(player, !Rank.isGM(player) && !Rank.isSubscriber(player));

        });

        player.getInventory().clear();
        ItemStack itemStack = new ItemStack(Material.COMPASS);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "Shard Selector");
        itemMeta.setLore(Arrays.asList("", ChatColor.GRAY.toString() + ChatColor.ITALIC + "Browse all available shards"));
        itemStack.setItemMeta(itemMeta);
        player.getInventory().setItem(0, itemStack);

        player.sendMessage("");
        CenteredMessage.sendCenteredMessage(player, ChatColor.GOLD.toString() + ChatColor.BOLD + "DUNGEON REALMS");
        player.sendMessage(new String[]{
                ChatColor.AQUA + "• " + ChatColor.DARK_AQUA + "Website: www.dungeonrealms.net",
                ChatColor.AQUA + "• " + ChatColor.DARK_AQUA + "Store: http://shop.dungeonrealms.net",
                ChatColor.AQUA + "• " + ChatColor.DARK_AQUA + "Select a shard to play on using your" + ChatColor.YELLOW + " Shard Selector"});
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        event.setQuitMessage(null);
        Player player = event.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(ServerLobby.getServerLobby(), () -> {
            if (DatabaseAPI.getInstance().PLAYERS.containsKey(player.getUniqueId()))
            {
                DatabaseAPI.getInstance().PLAYERS.remove(player.getUniqueId());
            }
        }, 1L);
    }
}
