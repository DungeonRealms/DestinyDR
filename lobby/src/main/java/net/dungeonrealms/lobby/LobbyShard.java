package net.dungeonrealms.lobby;

import lombok.Getter;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.common.network.enumeration.EnumShardType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Created by Giovanni on 5-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class LobbyShard
{
    @Getter
    private UUID uuid;

    public LobbyShard(UUID uuid)
    {
        this.uuid = uuid;
    }

    public List<BungeeServerInfo> getShardData()
    {
        List<BungeeServerInfo> servers = new ArrayList<>(ServerLobby.getServerLobby().getLobbyShard().getFilteredServers().values());

        Collections.sort(servers, (o1, o2) -> {

            int o1num = Integer.parseInt(o1.getServerName().substring(o1.getServerName().length() - 1));
            int o2num = Integer.parseInt(o2.getServerName().substring(o2.getServerName().length() - 1));

            if (!o1.getServerName().contains("us"))
                return -1;

            return o1num - o2num;
        });
        return servers;
    }

    public HashMap<Integer, String> getShardInfo(Player player)
    {
        List<BungeeServerInfo> servers = new ArrayList<>(getFilteredServers().values());

        Collections.sort(servers, (o1, o2) -> {

            int o1num = Integer.parseInt(o1.getServerName().substring(o1.getServerName().length() - 1));
            int o2num = Integer.parseInt(o2.getServerName().substring(o2.getServerName().length() - 1));

            if (!o1.getServerName().contains("us"))
                return -1;

            return o1num - o2num;
        });

        for (BungeeServerInfo info : servers)
        {
            String bungeeName = info.getServerName();
            String shardID = ShardInfo.getByPseudoName(bungeeName).getShardID();

            // Do not show YT / CS shards unless they've got the appropriate permission to see them.
            if ((shardID.contains("YT") && !Rank.isYouTuber(player)) || (shardID.contains("CS") && !Rank.isSupport(player)) || (shardID.equalsIgnoreCase("US-0") && !Rank.isGM(player)))
            {
                continue;
            }
        }
        HashMap<Integer, String> map = new HashMap<>();
        int i = 0;
        for (BungeeServerInfo server : servers)
        {
            String shardID = ShardInfo.getByPseudoName(server.getServerName()).getShardID();
            String load = server.getMotd1().replace("}", "").replace("\"", "").split(",")[1];
            int minPlayers = server.getOnlinePlayers();
            int maxPlayers = server.getMaxPlayers();
            String color = "";
            if (shardID.contains("SUB"))
            {
                color = "&a";
            }
            if (shardID.contains("BR"))
            {
                color = "&3";
            }
            if (shardID.contains("US"))
            {
                color = "&e";
            }
            if (shardID.contains("YT"))
            {
                color = "&6";
            }
            if (shardID.contains("EU"))
            {
                color = "&6";
            }
            if (shardID.contains("CS"))
            {
                color = "&c";
            }
            String shardString = color + shardID + " " + load + " &7(" + minPlayers + "/" + maxPlayers + ") ";
            map.put(i, ChatColor.translateAlternateColorCodes('&', shardString));
            i++;
        }
        return map;
    }

    private int getNormalServers()
    {
        int count = 0;

        for (String bungeeName : getFilteredServers().keySet())
        {
            String shardID = ShardInfo.getByPseudoName(bungeeName).getShardID();
            if (getServerType(shardID).equals(""))
                count++;
        }

        return count;
    }

    public ItemStack getShardItem(String shardID)
    {
        shardID = shardID.toUpperCase();

        if (shardID.equals("US-0")) return new ItemStack(Material.DIAMOND);
        else if (shardID.startsWith("CS-")) return new ItemStack(Material.PRISMARINE_SHARD);
        else if (shardID.startsWith("YT-")) return new ItemStack(Material.SPECKLED_MELON);
        else if (shardID.startsWith("BR-")) return new ItemStack(Material.SAPLING, 1, (byte) 3);
        else if (shardID.startsWith("SUB-")) return new ItemStack(Material.EMERALD);

        return new ItemStack(Material.END_CRYSTAL);
    }

    public ChatColor getShardColour(String shardID)
    {
        shardID = shardID.toUpperCase();

        if (shardID.equals("US-0")) return ChatColor.AQUA;
        else if (shardID.startsWith("CS-")) return ChatColor.BLUE;
        else if (shardID.startsWith("YT-")) return ChatColor.RED;
        else if (shardID.startsWith("SUB-")) return ChatColor.GREEN;

        return ChatColor.YELLOW;
    }

    public Map<String, BungeeServerInfo> getFilteredServers()
    {
        Map<String, BungeeServerInfo> filteredServers = new HashMap<>();

        for (Map.Entry<String, BungeeServerInfo> e : BungeeServerTracker.getTrackedServers().entrySet())
        {
            String bungeeName = e.getKey();
            if (ShardInfo.getByPseudoName(bungeeName) == null) continue;
            BungeeServerInfo info = e.getValue();

            if (!info.isOnline() || info.getMotd1().contains("offline"))
                continue;

            filteredServers.put(bungeeName, info);
        }

        return filteredServers;
    }

    public EnumShardType getServerType(String shardID)
    {
        if (shardID.contains("SUB")) return EnumShardType.SUBSCRIBER;
        if (shardID.contains("YT")) return EnumShardType.YOUTUBE;
        if (shardID.contains("BR")) return EnumShardType.BRAZILLIAN;
        if (shardID.contains("RP")) return EnumShardType.ROLEPLAY;
        if (shardID.contains("CS")) return EnumShardType.SUPPORT;
        return null;
    }
}
