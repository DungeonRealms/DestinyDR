package net.dungeonrealms.proxy.netty.lobby;

import lombok.Getter;
import net.dungeonrealms.proxy.DungeonBungee;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Giovanni on 1-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ProxyLobby
{
    @Getter
    private UUID uniqueId;

    public ProxyLobby(UUID uuid)
    {
        this.uniqueId = uuid;
    }

    public List<ServerInfo> bufferLobbies()
    {
        List<ServerInfo> servers = DungeonBungee.getDungeonBungee().getProxy().getServers().values().stream().filter(server -> {
            return server.getName().contains("Lobby");
        }).collect(Collectors.toList());
        Collections.sort(servers, (o1, o2) -> o1.getPlayers().size() - o2.getPlayers().size());
        return servers;
    }
}
