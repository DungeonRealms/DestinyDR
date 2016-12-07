package net.dungeonrealms.drproxy;

import net.dungeonrealms.drproxy.database.Database;
import net.dungeonrealms.drproxy.netty.NettyHandler;
import net.dungeonrealms.drproxy.player.PlayerManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by Evoltr on 11/30/2016.
 */
public class DRProxy extends Plugin {

    private static DRProxy instance;
    private Configuration config;
    private Database database;

    private NettyHandler nettyHandler;
    private PlayerManager playerManager;

    public static DRProxy getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();

        try {
            setupDatabase();
            log("Successfully set up database. DB: " + getDatabase().);
        } catch (Exception e) {
            log("Failed to set up database");
        }

        nettyHandler = new NettyHandler(this);
        playerManager = new PlayerManager(this);
    }

    @Override
    public void onDisable() {
        getNettyHandler().disconnect();
    }

    public static void log(String msg) {
        getInstance().getLogger().info(msg);
    }

    public String getProxyName() {
        return getConfig().getString("name");
    }

    public void loadConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try {
                Files.copy(getResourceAsStream("config.yml"), file.toPath());
            } catch (IOException e) {
                getLogger().info("Failed to create the config: " + e.getMessage());
            }
        }

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            getLogger().info("Failed to load the config: " + e.getMessage());
        }
    }

    public void setupDatabase() {
        database = new Database(this);

        // Run this async because bungee won't allow thread creation on the main thread.
        ProxyServer.getInstance().getScheduler().runAsync(this, () -> database.setup());
    }

    public Database getDatabase() {
        return database;
    }

    public Configuration getConfig() {
        return config;
    }

    public NettyHandler getNettyHandler() {
        return nettyHandler;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
