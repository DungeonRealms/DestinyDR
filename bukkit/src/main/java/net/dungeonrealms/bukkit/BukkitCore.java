package net.dungeonrealms.bukkit;

import net.dungeonrealms.bukkit.database.Database;
import net.dungeonrealms.bukkit.managers.NetworkManager;
import net.dungeonrealms.bukkit.player.PlayerManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Evoltr on 12/3/2016.
 */
public class BukkitCore extends JavaPlugin {

    private static BukkitCore instance;
    private NetworkManager networkManager;
    private Database database;
    private PlayerManager playerManager;

    public static void log(String msg) {
        getInstance().getLogger().info(msg);
    }

    public static BukkitCore getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        networkManager = new NetworkManager(this);
        playerManager = new PlayerManager(this);

        database = new Database(this);
        database.setup();

        log("GameManager has successfully loaded and is taking over your bukkit server.");
    }

    @Override
    public void onDisable() {
        instance = null;

        getNetworkManager().disconnect();
    }

    public String getServerName() {
        return getConfig().getString("name");
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public Database getDB() {
        return database;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
