package net.dungeonrealms.proxy.netty.shard;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.common.old.network.ShardInfo;
import net.dungeonrealms.common.old.network.enumeration.EnumShardType;
import net.dungeonrealms.proxy.DungeonBungee;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by Giovanni on 1-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ProxyShard {
    @Getter
    private UUID uniqueId;

    public ProxyShard(UUID uuid) {
        this.uniqueId = uuid;
    }

    public List<ServerInfo> bufferShards(boolean populated, boolean premium) {
        List<ServerInfo> servers = Lists.newArrayList();

        for (ShardInfo shardInfo : ShardInfo.values()) {
            EnumShardType shardType = shardInfo.getShardType();
            // They must meet the criteria, is it a beta shard or if the player is premium and the requested shard type is sub, sir yes sir!
            if (shardType == EnumShardType.BETA || (premium && shardType == EnumShardType.SUBSCRIBER)) {
                // Excluding the Brazillian shard for searching optimal shards, same goes for YT/STAFF/SUPPORT etc shards, no racismo, señor
                servers.add(DungeonBungee.getDungeonBungee().getProxy().getServerInfo(shardInfo.getPseudoName())); // Get shard by name
            }
        }

        Collections.sort(servers, (par1, par2) -> par1.getPlayers().size() - par2.getPlayers().size());
        if (populated) {
            Collections.reverse(servers); // Seems like that's not going to happen today mate
        }
        return servers;
    }
}
