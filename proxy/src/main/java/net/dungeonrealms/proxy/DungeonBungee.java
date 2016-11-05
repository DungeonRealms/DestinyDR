package net.dungeonrealms.proxy;

import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;

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

    @Getter
    private File channelFile;

    @Override
    public void onEnable()
    {
        dungeonBungee = this;

        this.console = this.getProxy().getConsole();

        if (!this.getDataFolder().exists())
        {
            this.getDataFolder().mkdir();
        }

        this.channelFile = new File(DungeonBungee.getDungeonBungee().getDataFolder().getPath(), "channel.yml");

        if (!this.channelFile.exists())
        {
            try
            {
                this.channelFile.createNewFile();
                try (InputStream inputStream = getResourceAsStream("channel.yml"))
                {
                    try (OutputStream outputStream = new FileOutputStream(this.channelFile))
                    {
                        ByteStreams.copy(inputStream, outputStream);
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            this.networkProxy = new NetworkProxy(ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.channelFile));
        } catch (IOException e)
        {
            e.printStackTrace();
            this.getProxy().stop(); // No channel configuration, no purpose
        }

        this.networkProxy.deploy();
    }

    @Override
    public void onDisable()
    {
        this.networkProxy.undeploy();
    }
}
