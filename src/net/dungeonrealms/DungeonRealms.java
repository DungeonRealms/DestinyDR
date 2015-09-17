package net.dungeonrealms;

import net.dungeonrealms.listeners.MainListener;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mongo.DatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Nick on 9/17/2015.
 */
public class DungeonRealms extends JavaPlugin {

    static DungeonRealms instance = null;

    public static DungeonRealms getInstance() {
        return instance;
    }

    public void onLoad() {
        Utils.log.info("DungeonRealms onLoad() ... STARTING UP");
        instance = this;
    }

    public void onEnable() {
        long START_TIME = System.currentTimeMillis() / 1000L;
        Utils.log.info("DungeonRealms onEnable() ... STARTING UP");
        DatabaseAPI.getInstance().startInitialization();

        PluginManager pm = Bukkit.getPluginManager();
        Utils.log.info("DungeonRealms Registering Events() ... STARTING ...");
        pm.registerEvents(new MainListener(), this);
        Utils.log.info("DungeonRealms Registering Events() ... FINISHED!");
        Utils.log.info("DungeonRealms STARTUP FINISHED in ... " + ((System.currentTimeMillis() / 1000l) / START_TIME) + "s/");

    }

    public void onDisable() {
        Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN");
    }

}
