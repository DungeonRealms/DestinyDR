package net.dungeonrealms.spawning;

import net.dungeonrealms.DungeonRealms;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;

/**
 * Created by Chase on Sep 28, 2015
 */
public class SpawningMechanics {
    public static ArrayList<MobSpawner> SPAWNERS = new ArrayList<>();
    public static ArrayList<String> SPANWER_CONFIG = new ArrayList<>();

    public static void updateSpawners() {
        if (SPAWNERS.size() > 0)
            SPAWNERS.stream().forEach(net.dungeonrealms.spawning.MobSpawner::spawnIn);
    }

    public static ArrayList<MobSpawner> getSpawners() {
        return SPAWNERS;
    }

    public static void add(MobSpawner spawner) {
        SPAWNERS.add(spawner);
    }

    public static void killAll() {
        SPAWNERS.forEach(net.dungeonrealms.spawning.MobSpawner::kill);
    }

    public static void loadSpawners() {
        SPANWER_CONFIG = (ArrayList<String>) DungeonRealms.getInstance().getConfig().getStringList("spawners");
    	for(String line : SPANWER_CONFIG){
    		String[] coords = line.split("=")[0].split(",");
    		double x,y,z;
    		x = Double.parseDouble(coords[0]);
    		y = Double.parseDouble(coords[1]);
    		z = Double.parseDouble(coords[2]);
    		int tier = Integer.parseInt(line.split(":")[1]);
    		String monster = line.split("=")[1].split(":")[0];
    		MobSpawner spawner = new MobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier);
            SPAWNERS.add(spawner);
    	}
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), SpawningMechanics::updateSpawners, 0, 10 * 20L);
    }

    /**
     * @param i
     */
    public static void remove(int i) {
        SPAWNERS.remove(i);
    }

    /**
     * @param mobSpawner
     */
    public static void remove(MobSpawner mobSpawner) {
        SPAWNERS.remove(mobSpawner);
    }
}
