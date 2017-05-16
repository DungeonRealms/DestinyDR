package net.dungeonrealms.game.world.spawning;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base Mob Spawner - Spawns regular custom entities.
 *
 * Redone by Kneesnap on April 20th, 2017.
 */
public class BaseMobSpawner extends MobSpawner {

    private Map<Entity, Integer> respawnTimes = new ConcurrentHashMap<>();

    public BaseMobSpawner(Location location, EnumMonster type, String name, int tier, int spawnAmount, String power, int respawnDelay, int mininmumXZ, int maximumXZ) {
        super(location, type, name, tier, spawnAmount, power, respawnDelay, mininmumXZ, maximumXZ);
    }

    @Override
    protected void createMobs() {
    	// The delay of when monsters should spawn is set after a monster dies.
		// We don't want excess monsters spawning.
    	getSpawnedMonsters().stream().filter(ent -> ent != null && ent.isDead()).forEach(monster ->
			respawnTimes.put(monster, getRespawnDelay() + (new Random().nextInt(getRespawnDelay() / 2) + 15)));

    	if (isFirstSpawn())
    		for (int i = 0; i < 2; i++)
    			spawn();

    	super.createMobs();
    }

    /**
     * Initialize spawner
     */
    public void init() {
    	Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> {
    		boolean nearby = GameAPI.arePlayersNearbyAsync(getLocation(), 32);

    		if (nearby) {
    			// Spawn some monsters in.
    			if (getTimerID() == -1) //Confirm we're not already spawning monsters.
    				setTimerID(Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), this::spawnIn, 35L).getTaskId());
    		}else if (getTimerID() != -1) {
    			// If no players are around
    			Bukkit.getScheduler().cancelTask(getTimerID());
    			setFirstSpawn(true); //The next player to come by should have some monsters spawn.
    			setTimerID(-1);
    		}
    	}, 0L, 40L);
    }

    @Override
    protected boolean canSpawnMobs() {
    	if (isFirstSpawn()) {
    		respawnTimes.clear();
    		return true;
    	}

    	if (!respawnTimes.isEmpty()) {
    		for (Entity e : respawnTimes.keySet()) {
    			int newTime = respawnTimes.get(e) - 1;
    			if (newTime > 0) {
    				respawnTimes.put(e, newTime);
    			} else {
    				respawnTimes.remove(e);
    				return true;
    			}
    		}
    		return false;
    	}

    	return super.canSpawnMobs();
    }
}
