package net.dungeonrealms.proxy;

import lombok.Getter;
import net.dungeonrealms.proxy.constant.ProxyConstants;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.IOException;

/**
 * Created by Giovanni on 1-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class DungeonBungee extends Plugin
{
    @Getter
    private static DungeonBungee dungeonBungee;

    @Getter
    private NetworkProxy networkProxy; // Main handle

    @Getter
    private CommandSender console;

    @Override
    public void onEnable()
    {
        dungeonBungee = this;

        this.console = this.getProxy().getConsole();

        try
        {
            this.networkProxy = new NetworkProxy().deploy(ConfigurationProvider.getProvider(YamlConfiguration.class).load(ProxyConstants.proxyConfiguration));
        } catch (IOException e)
        {
            e.printStackTrace();
            this.getProxy().stop(); // No channel configuration, no purpose
        }
    }

    @Override
    public void onDisable()
    {
        try
        {
            // Save networkProxy constants
            this.networkProxy.undeploy(ConfigurationProvider.getProvider(YamlConfiguration.class).load(ProxyConstants.proxyConfiguration));
        } catch (IOException e)
        {
            e.printStackTrace();
            // It's stopping anyway
        }
    }
}
