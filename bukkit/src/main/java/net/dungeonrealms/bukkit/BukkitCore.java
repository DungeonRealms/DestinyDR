package net.dungeonrealms.bukkit;

import lombok.Getter;
import net.dungeonrealms.bukkit.managers.NetworkManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Evoltr on 12/3/2016.
 */
public class BukkitCore extends JavaPlugin {

    @Getter
    private static BukkitCore instance;
    @Getter
    private NetworkManager networkManager;

    public static void log(String msg) {
        getInstance().getLogger().info(msg);
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.networkManager = new NetworkManager(this);

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

}
