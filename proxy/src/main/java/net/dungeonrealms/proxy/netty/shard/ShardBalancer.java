package net.dungeonrealms.proxy.netty.shard;

import lombok.Getter;
import net.dungeonrealms.common.network.ServerAddress;
import net.dungeonrealms.common.network.ping.PingResponse;
import net.dungeonrealms.common.network.ping.ServerPinger;
import net.dungeonrealms.proxy.DungeonBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Iterator;
import java.util.UUID;

/**
 * Created by Giovanni on 1-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ShardBalancer
{
    @Getter
    private UUID uniqueId;

    @Getter
    private boolean populatedHolder;

    @Getter
    private boolean premiumHolder;

    @Getter
    private boolean lobbyRedirector;

    public ShardBalancer(UUID uuid, boolean populated, boolean premium, boolean sendToLobby)
    {
        this.uniqueId = uuid;
        this.populatedHolder = populated;
        this.premiumHolder = premium;
        this.lobbyRedirector = sendToLobby;
    }

    public void handle()
    {
        DungeonBungee.getDungeonBungee().getProxy().getScheduler().runAsync(DungeonBungee.getDungeonBungee(), () -> {
            ProxiedPlayer player = DungeonBungee.getDungeonBungee().getProxy().getPlayer(this.uniqueId);
            Iterator<ServerInfo> optimalShardFinder = DungeonBungee.getDungeonBungee().getNetworkProxy().getProxyShard().bufferShards(this.premiumHolder, this.populatedHolder).iterator();
            while (optimalShardFinder.hasNext())
            {
                ServerInfo target = optimalShardFinder.next();

                try
                {
                    PingResponse ping = null;
                    boolean isOnline = true;

                    try
                    {
                        ping = ServerPinger.fetchData(new ServerAddress(target.getAddress().getHostName(), target.getAddress().getPort()), 024); // Octal
                    } catch (Exception e)
                    {
                        isOnline = true;
                    }
                    // No sessions, back to the lobby
                    if ((ping != null ? ping.getDescription().getText().contains("offline") : false))
                    {
                        if (!optimalShardFinder.hasNext())
                        {
                            if (this.lobbyRedirector)
                            {
                                player.connect(DungeonBungee.getDungeonBungee().getProxy().getServerInfo("Lobby"));
                            }
                            player.sendMessage(DungeonBungee.getDungeonBungee().getNetworkProxy().getProxyName()
                                    + ChatColor.RED.toString() + ChatColor.BOLD + "No shard session found, please retry");
                            return;
                        }
                        continue;
                    } else if (!isOnline)
                    {
                        if (!optimalShardFinder.hasNext())
                        {
                            if (this.lobbyRedirector)
                            {
                                player.connect(DungeonBungee.getDungeonBungee().getProxy().getServerInfo("Lobby"));
                            }
                            player.sendMessage(DungeonBungee.getDungeonBungee().getNetworkProxy().getProxyName()
                                    + ChatColor.RED.toString() + ChatColor.BOLD + "No shard session found, please retry");
                            return;
                        }
                        continue;
                    }
                } catch (Exception e)
                {
                    if (!optimalShardFinder.hasNext())
                    {
                        if (this.lobbyRedirector)
                        {
                            player.connect(DungeonBungee.getDungeonBungee().getProxy().getServerInfo("Lobby"));
                        }
                        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Unable to find a session for you.");
                        return;
                    }
                    continue;
                }
                if (!(player.getServer() != null && player.getServer().getInfo().equals(target)))
                    if (target.canAccess(player))
                    {
                        player.sendMessage(DungeonBungee.getDungeonBungee().getNetworkProxy().getProxyName() + ChatColor.GRAY.toString() + ChatColor.BOLD + "Moving your current session...");
                        player.connect(target);
                        break;
                    } else
                    {
                        if (!optimalShardFinder.hasNext())
                        {
                            if (this.lobbyRedirector)
                            {
                                player.connect(DungeonBungee.getDungeonBungee().getProxy().getServerInfo("Lobby"));
                            }
                            player.sendMessage(DungeonBungee.getDungeonBungee().getNetworkProxy().getProxyName()
                                    + ChatColor.RED.toString() + ChatColor.BOLD + "No shard session found, please retry");
                            return;
                        }
                    }
                else
                {
                    if (!optimalShardFinder.hasNext())
                    {
                        if (this.lobbyRedirector)
                        {
                            player.connect(DungeonBungee.getDungeonBungee().getProxy().getServerInfo("Lobby"));
                        }
                        player.sendMessage(DungeonBungee.getDungeonBungee().getNetworkProxy().getProxyName()
                                + ChatColor.RED.toString() + ChatColor.BOLD + "No shard session found, please retry");
                        return;
                    }
                }
            }
        });
    }
}
