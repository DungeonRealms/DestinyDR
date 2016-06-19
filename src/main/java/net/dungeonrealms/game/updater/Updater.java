package net.dungeonrealms.game.updater;

import net.dungeonrealms.DungeonRealms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public abstract class Updater implements Runnable {

    protected DungeonRealms plugin = DungeonRealms.getInstance();

    private Object obj;
    private int task;
    private static Set<Updater> updaters = new HashSet<>();

    public Updater(JavaPlugin plugin, long interval, Object obj) {
        this.obj = obj;
        this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 20L, interval);
        updaters.add(this);
    }

    public Updater(JavaPlugin plugin, long startDelay, long interval, Object obj) {
        this.obj = obj;
        this.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, startDelay, interval);
        updaters.add(this);
    }


    public static Updater getUpdater(Object obj) {
        for (Updater updater : updaters) {

            if (updater.getID() == null) continue;

            if (((updater.getID() instanceof String)) &&
                    (((String) updater.getID()).equalsIgnoreCase((String) obj))) {
                return updater;
            }
            if (updater.getID().equals(obj)) {
                return updater;
            }
        }
        return null;
    }

    public Object getID() {
        return this.obj;
    }

    public void cancel() {
        Bukkit.getScheduler().cancelTask(this.task);
        updaters.remove(this);
    }
}
