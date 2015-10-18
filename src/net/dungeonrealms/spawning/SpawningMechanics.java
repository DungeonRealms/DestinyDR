package net.dungeonrealms.spawning;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import net.dungeonrealms.DungeonRealms;

/**
 * Created by Chase on Sep 28, 2015
 */
public class SpawningMechanics {
    private static ArrayList<MobSpawner> ALLSPAWNERS = new ArrayList<>();
    public static ArrayList<String> SPANWER_CONFIG = new ArrayList<>();

    
    public static void initSpawners(){
     		ALLSPAWNERS.forEach(spawner -> Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> spawner.init(), 20l));
    }
    
    public static ArrayList<MobSpawner> getSpawners() {
        return ALLSPAWNERS;
    }

    public static void add(MobSpawner spawner) {
        ALLSPAWNERS.add(spawner);
    }

    public static void killAll() {
        for(MobSpawner spawner : ALLSPAWNERS){
        	spawner.kill();
        	spawner.armorstand.getBukkitEntity().remove();
        	spawner.armorstand.getWorld().removeEntity(spawner.armorstand);
        }
    }

    public static void loadSpawners() {
    	
    	for(Entity ent : Bukkit.getWorlds().get(0).getEntities()){
    		ent.remove();
    	}
        SPANWER_CONFIG = (ArrayList<String>) DungeonRealms.getInstance().getConfig().getStringList("spawners");
    	for(String line : SPANWER_CONFIG){
    		String[] coords = line.split("=")[0].split(",");
    		double x, y,z;
    		x = Double.parseDouble(coords[0]);
    		y = Double.parseDouble(coords[1]);
    		z = Double.parseDouble(coords[2]);
    		String tierString = line.substring(line.indexOf(":"), line.indexOf(";"));
    		tierString = tierString.substring(1);
    		int tier = Integer.parseInt(tierString);
    		int spawnAmount = Integer.parseInt(line.split(";")[1]);
    		String monster = line.split("=")[1].split(":")[0];
    		MobSpawner spawner = new MobSpawner(new Location(Bukkit.getWorlds().get(0), x, y, z), monster, tier, spawnAmount);
            ALLSPAWNERS.add(spawner);
    	}
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), SpawningMechanics::initSpawners, 0, 4 * 20L);
    }

    /**
     * @param i
     */
    public static void remove(int i) {
        ALLSPAWNERS.remove(i);
    }

    /**
     * @param mobSpawner
     */
    public static void remove(MobSpawner mobSpawner) {
        ALLSPAWNERS.remove(mobSpawner);
    }
}
