package net.dungeonrealms.lobby.old;

import lombok.Getter;
import net.dungeonrealms.common.old.network.ShardInfo;
import net.dungeonrealms.common.old.network.bungeecord.BungeeServerInfo;
import net.dungeonrealms.common.old.network.bungeecord.BungeeServerTracker;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Giovanni on 5-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class LobbyShard {
    @Getter
    private UUID uuid;

    public LobbyShard(UUID uuid) {
        this.uuid = uuid;
    }

    public ConcurrentHashMap<String, BungeeServerInfo> collectShards() {
        ConcurrentHashMap<String, BungeeServerInfo> collectedShards = new ConcurrentHashMap<>();
        for (BungeeServerInfo serverInfo : getShardData()) {
            collectedShards.put(serverInfo.getServerName(), serverInfo);
        }
        return collectedShards;
    }

    public BungeeServerInfo getServerInfo(String shard) {
        return collectShards().get(shard);
    }

    public List<BungeeServerInfo> getShardData() {
        List<BungeeServerInfo> servers = new ArrayList<>(ServerLobby.getServerLobby().getLobbyShard().getFilteredServers().values());

        Collections.sort(servers, (o1, o2) ->
        {
            int o1num = Integer.parseInt(o1.getServerName().substring(o1.getServerName().length() - 1));
            int o2num = Integer.parseInt(o2.getServerName().substring(o2.getServerName().length() - 1));

            if (!o1.getServerName().contains("us"))
                return -1;

            return o1num - o2num;
        });
        return servers;
    }

    public ItemStack getShardItem(String shardID) {
        shardID = shardID.toUpperCase();

        if (shardID.equals("US-0")) return new ItemStack(Material.DIAMOND);
        else if (shardID.startsWith("CS-")) return new ItemStack(Material.PRISMARINE_SHARD);
        else if (shardID.startsWith("YT-")) return new ItemStack(Material.SPECKLED_MELON);
        else if (shardID.startsWith("BR-")) return new ItemStack(Material.SAPLING, 1, (byte) 3);
        else if (shardID.startsWith("SUB-")) return new ItemStack(Material.EMERALD);

        return new ItemStack(Material.END_CRYSTAL);
    }

    public ChatColor getShardColour(String shardID) {
        shardID = shardID.toUpperCase();

        if (shardID.equals("US-0")) return ChatColor.AQUA;
        else if (shardID.startsWith("CS-")) return ChatColor.BLUE;
        else if (shardID.startsWith("YT-")) return ChatColor.RED;
        else if (shardID.startsWith("SUB-")) return ChatColor.GREEN;

        return ChatColor.YELLOW;
    }

    public Map<String, BungeeServerInfo> getFilteredServers() {
        Map<String, BungeeServerInfo> filteredServers = new HashMap<>();

        for (Map.Entry<String, BungeeServerInfo> e : BungeeServerTracker.getTrackedServers().entrySet()) {
            String bungeeName = e.getKey();
            if (ShardInfo.getByPseudoName(bungeeName) == null) continue;
            BungeeServerInfo info = e.getValue();

            if (!info.isOnline() || info.getMotd1().contains("offline"))
                continue;

            filteredServers.put(bungeeName, info);
        }

        return filteredServers;
    }
}
