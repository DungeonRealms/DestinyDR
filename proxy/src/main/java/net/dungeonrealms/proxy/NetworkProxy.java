package net.dungeonrealms.proxy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.old.network.ShardInfo;
import net.dungeonrealms.network.GameClient;
import net.dungeonrealms.network.awt.EnumProxyHolder;
import net.dungeonrealms.network.awt.Proxy;
import net.dungeonrealms.proxy.handle.ProxyHandler;
import net.dungeonrealms.proxy.netty.lobby.ProxyLobby;
import net.dungeonrealms.proxy.netty.shard.ProxyShard;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Giovanni on 1-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class NetworkProxy implements Proxy
{
    @Getter
    private GameClient gameClient;

    @Getter
    @Setter
    private boolean maintenance;

    @Getter
    private List<String> whitelist;

    @Getter
    private ProxyHandler handlerCore;

    @Getter
    private ProxyShard proxyShard;

    @Getter
    private ProxyLobby proxyLobby;

    @Getter
    private String proxyName = ChatColor.translateAlternateColorCodes('&', "&cLIMBO > ");

    @Getter
    private EnumProxyHolder proxyHolder;

    @Getter
    private Configuration configuration;

    public NetworkProxy(Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void deploy()
    {
        this.readConfiguration();
        this.connect();
        this.handlerCore = new ProxyHandler();
        this.handlerCore.prepare();
        DungeonBungee.getDungeonBungee().getConsole().sendMessage("DEBUG: " + Constants.MOTD);
    }

    protected void undeploy()
    {
        configuration.set("network.maintenance", this.maintenance);
        configuration.set("network.holder", this.proxyHolder.name());
        configuration.set("channel.developers", this.whitelist);
        configuration.set("channel.proxyName", this.proxyName);
        try
        {
            // Save the configuration
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, DungeonBungee.getDungeonBungee().getChannelFile());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void readConfiguration()
    {
        DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.GREEN + "Reading proxy configuration..");
        if (!configuration.getKeys().isEmpty())
        {
            this.maintenance = configuration.getBoolean("network.maintenance");
            this.proxyHolder = EnumProxyHolder.valueOf(configuration.getString("network.holder"));
            this.whitelist = configuration.getStringList("channel.developers");
            this.proxyName = ChatColor.translateAlternateColorCodes('&', configuration.getString("channel.proxyName"));
        } else
        {
            configuration.set("network.maintenance", false);
            configuration.set("network.holder", EnumProxyHolder.MASTER.name());
            configuration.set("channel.developers", Arrays.asList("Atlas__", "Vawke"));
            configuration.set("channel.proxyName", "&cLIMBO > ");
            try
            {
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, DungeonBungee.getDungeonBungee().getChannelFile());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.GREEN + "> Finished");
    }

    private void connect()
    {
        this.gameClient = new GameClient();
        this.proxyShard = new ProxyShard(UUID.randomUUID());
        this.proxyLobby = new ProxyLobby(UUID.randomUUID());
        try
        {
            DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.GREEN + "Connecting to the master server..");
            // Collect all shards, connect them
            Arrays.stream(ShardInfo.values()).forEach(info ->
                    {
                        ServerInfo serverInfo;
                        serverInfo = ProxyServer.getInstance().constructServerInfo(info.getPseudoName(),
                                new InetSocketAddress(info.getAddress().getAddress(), info.getAddress().getPort()), "", false);
                        ProxyServer.getInstance().getServers().put(info.getPseudoName(), serverInfo);
                        DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.YELLOW + "Shard info constructed for: " + serverInfo.getName().toUpperCase());
                    }
            );
            this.gameClient.connect();
            DungeonBungee.getDungeonBungee().getConsole().sendMessage(ChatColor.GREEN + "> Connected");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void sendGlobalPacket(String task, String... contents)
    {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(task);

        for (String s : contents)
        {
            out.writeUTF(s);
        }

        this.gameClient.sendTCP(out.toByteArray());
    }

    @Override
    public EnumProxyHolder getProxyHolder()
    {
        return proxyHolder;
    }
}
