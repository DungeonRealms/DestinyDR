package net.dungeonrealms.lobby.misc.gui;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.menu.AbstractMenu;
import net.dungeonrealms.common.game.menu.construct.BasicGUI;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.lobby.ServerLobby;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Giovanni on 5-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ShardGUI extends BasicGUI
{
    public ShardGUI(Player player)
    {
        super(null, "Shard Selector", 9);

        List<BungeeServerInfo> servers = ServerLobby.getServerLobby().getLobbyShard().getShardData();

        if (!servers.isEmpty())
        {
            for (BungeeServerInfo serverInfo : servers)
            {
                String name = serverInfo.getServerName();
                String identifier = ShardInfo.getByPseudoName(name).getShardID();

                if (serverInfo.isOnline() && !serverInfo.getMotd1().contains("offline"))
                {
                    // Construct the lore
                    List<String> defaultLore = Lists.newArrayList();
                    defaultLore.add(ChatColor.GREEN + "Shard is online!");
                    defaultLore.add("");
                    List<String> connectionLore = Lists.newArrayList();
                    connectionLore.add(ChatColor.GREEN + "▶ Click to connect");
                    ChatColor countColor = ChatColor.GREEN;
                    if (serverInfo.getOnlinePlayers() > serverInfo.getMaxPlayers() - 11)
                    {
                        countColor = ChatColor.RED;
                    }
                    if (serverInfo.getOnlinePlayers() > serverInfo.getMaxPlayers() - 18)
                    {
                        countColor = ChatColor.YELLOW;
                    }
                    if (serverInfo.getOnlinePlayers() >= serverInfo.getMaxPlayers())
                    {
                        connectionLore.clear();
                        connectionLore.add(ChatColor.RED + "Shard is full!");
                        connectionLore.add(ChatColor.RED + "Purchase subscriber to join full shards");
                        countColor = ChatColor.DARK_RED;
                    }

                    defaultLore.add(ChatColor.GRAY + "Players: " + countColor + serverInfo.getOnlinePlayers() + ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + serverInfo.getMaxPlayers());
                    defaultLore.add("");

                    ItemStack itemStack = new ItemStack(ServerLobby.getServerLobby().getLobbyShard().getShardItem(identifier));
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(ServerLobby.getServerLobby().getLobbyShard().getShardColour(identifier).toString() + ChatColor.BOLD + identifier);

                    // Check if the player has the required rank to view specific shards
                    if (identifier.contains("YT") && !Rank.isYouTuber(player) || identifier.contains("CS") && !Rank.isSupport(player) || identifier.contains("US-0") && !Rank.isDev(player))
                    {
                        connectionLore.clear();
                        connectionLore.add(ChatColor.GRAY + "You can't connect to this shard");
                    }

                    defaultLore.addAll(connectionLore); // Merge them
                    itemMeta.setLore(defaultLore);
                    itemStack.setItemMeta(itemMeta);

                    this.addItem(itemStack, this.getInventory().firstEmpty());
                }
            }
        }
    }
}
