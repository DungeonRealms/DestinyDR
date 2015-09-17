package net.dungeonrealms;

import net.dungeonrealms.mastery.Utils;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Nick on 9/17/2015.
 */
public class DungeonRealms extends JavaPlugin {

    public void onLoad() {
        Utils.log.info("DungeonRealms onLoad() ... STARTING UP");
    }

    public void onEnable() {
        Utils.log.info("DungeonRealms onEnable() ... STARTING UP");
    }

    public void onDisable() {
        Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN");
    }

}
