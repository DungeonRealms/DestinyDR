package xyz.dungeonrealms;

import org.bukkit.plugin.java.JavaPlugin;
import xyz.dungeonrealms.mechanics.MechanicManager;

import java.util.logging.Logger;

/**
 * Created by Nick on 12/10/2015.
 */
public final class DungeonRealms extends JavaPlugin {

    private static DungeonRealms instance = null;

    public static DungeonRealms getInstance() {
        if (instance == null) {
            log.warning("Unable to provide instance of main class!");
            return null;
        }
        return instance;
    }

    public static Logger log = Logger.getLogger("DungeonRealms | ");

    public MechanicManager manager;

    public void onEnable() {

        manager = new MechanicManager();

        manager.registerMechanic(null);


        manager.loadMechanics();

    }

    public void onDisable() {

    }


}
