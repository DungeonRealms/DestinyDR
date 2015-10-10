package net.dungeonrealms.spawning;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.dungeonrealms.DungeonRealms;

/**
 * Created by Chase on Sep 28, 2015
 */
public class SpawningMechanics {
    private static ArrayList<MobSpawner> spawners = new ArrayList<>();
    public static ArrayList<String> spawnerConfig = new ArrayList<>();

    public static void updateSpawners() {
        if (spawners.size() > 0)
            spawners.stream().forEach(net.dungeonrealms.spawning.MobSpawner::spawnIn);
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
    	spawnerConfig = (ArrayList<String>) DungeonRealms.getInstance().getConfig().getStringList("spawners");
    	for(String line : spawnerConfig){
    		String[] coords = line.split("=")[0].split(",");
    		double x,y,z = 0;
    		x = Double.parseDouble(coords[0]);
    		y = Double.parseDouble(coords[1]);
    		z = Double.parseDouble(coords[2]);
    		int tier = Integer.parseInt(line.split(":")[1]);
    		String monster = line.split("=")[1].split(":")[0];
    		MobSpawner spawner = new MobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier);
    		spawners.add(spawner);
    	}
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
