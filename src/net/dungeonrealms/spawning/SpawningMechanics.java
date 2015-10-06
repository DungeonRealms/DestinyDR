package net.dungeonrealms.spawning;

import net.dungeonrealms.DungeonRealms;
import org.bukkit.Bukkit;

import java.util.ArrayList;

/**
 * Created by Chase on Sep 28, 2015
 */
public class SpawningMechanics {
    private static ArrayList<MobSpawner> spawners = new ArrayList<>();

    public static void updateSpawners() {
        if (spawners.size() > 0)
            spawners.stream().filter(MobSpawner::playersAround).forEach(net.dungeonrealms.spawning.MobSpawner::spawnIn);
    }

    public static ArrayList<MobSpawner> getSpawners() {
        return spawners;
    }

    public static void add(MobSpawner spawner) {
        spawners.add(spawner);
    }

    public static void killAll() {
        spawners.forEach(net.dungeonrealms.spawning.MobSpawner::kill);
    }

    public static void loadSpawners() {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), SpawningMechanics::updateSpawners, 0, 10 * 20L);
    }

    /**
     * @param i
     */
    public static void remove(int i) {
        spawners.remove(i);
    }

    /**
     * @param mobSpawner
     */
    public static void remove(MobSpawner mobSpawner) {
        spawners.remove(mobSpawner);
    }
}
