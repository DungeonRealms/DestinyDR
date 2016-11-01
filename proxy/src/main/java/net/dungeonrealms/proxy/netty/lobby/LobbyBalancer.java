package net.dungeonrealms.proxy.netty.lobby;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.proxy.DungeonBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Iterator;

/**
 * Created by Giovanni on 1-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class LobbyBalancer
{
    @Getter
    private ProxiedPlayer proxiedPlayer;

    public LobbyBalancer(ProxiedPlayer proxiedPlayer)
    {
        this.proxiedPlayer = proxiedPlayer;
    }

    public void handle()
    {
        Iterator<ServerInfo> optimalLobbies = DungeonBungee.getDungeonBungee().getNetworkProxy().getProxyLobby().bufferLobbies().iterator();
        while (optimalLobbies.hasNext())
        {
            ServerInfo target = optimalLobbies.next();
            if ((this.proxiedPlayer.getServer() != null && this.proxiedPlayer.getServer().getInfo().equals(target)))
            {
                if (!optimalLobbies.hasNext())
                {
                    this.proxiedPlayer.disconnect(DungeonBungee.getDungeonBungee().getNetworkProxy().getProxyName() + " No lobby session found, please retry");
                    return;
                }
            } else
            {
                try
                {
                    this.proxiedPlayer.connect(target);
                } catch (Exception e)
                {
                    if (!optimalLobbies.hasNext())
                        this.proxiedPlayer.disconnect(DungeonBungee.getDungeonBungee().getNetworkProxy().getProxyName() + " No lobby session found, please retry");
                    else continue;
                }

                break;
            }

        }
    }
}
