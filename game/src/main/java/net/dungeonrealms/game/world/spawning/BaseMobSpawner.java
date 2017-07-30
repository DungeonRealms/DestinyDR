package net.dungeonrealms.game.world.spawning;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base Mob Spawner - Spawns regular custom entities.
 * <p>
 * Redone by Kneesnap on April 20th, 2017.
 */
public class BaseMobSpawner extends MobSpawner {

    private Map<Entity, Integer> respawnTimes = new ConcurrentHashMap<>();

    public BaseMobSpawner(Location location, EnumMonster type, String name, int tier, int spawnAmount, String power, int respawnDelay, int mininmumXZ, int maximumXZ, int minMobScore, int maxMobScore) {
        super(location, type, name, tier, spawnAmount, power, respawnDelay, mininmumXZ, maximumXZ, minMobScore, maxMobScore);
    }

    @Override
    protected void createMobs() {
        // The delay of when monsters should spawn is set after a monster dies.
        // We don't want excess monsters spawning.
//        getSpawnedMonsters().stream().filter(ent -> ent != null && ent.isDead()).forEach(monster -> {
//            int time = getRespawnDelay() + ThreadLocalRandom.current().nextInt(getRespawnDelay() / 2) + 15;
//            System.out.println("Time: " + time + " setting to " + monster.getName());
//            respawnTimes.put(monster, time);
//        });

        if (isFirstSpawn() && getSpawnAmount() > 1) {
            int leftOver = toRespawn - 1;

            for (int i = 0; i < (isDungeon() ? getSpawnAmount() - 1 : leftOver > 0 ? leftOver : getSpawnAmount() - 1); i++)
                spawn();
        }

        super.createMobs();
    }

    @Override
    public MobSpawner clone() {
        BaseMobSpawner spawner =  new BaseMobSpawner(getLocation(), getMonsterType(), getCustomName(), getTier(), getSpawnAmount(), getLvlRange(),
                getInitialRespawnDelay(), getMinimumXZ(), getMaximumXZ(), getMinMobScore(), getMaxMobScore());
        if(isDungeon())
            spawner.setDungeon(isDungeon());
        return spawner;
    }

    /**
     * Initialize spawner
     */
    public void init() {
        if (spawnerTask != null && Bukkit.getScheduler().isCurrentlyRunning(spawnerTask.getTaskId())) {
            spawnerTask.cancel();
            Bukkit.getLogger().info("Cancelling already running spawner task: " + spawnerTask.getTaskId());
        }

        spawnerTask = Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> {
            boolean nearby = GameAPI.arePlayersNearbyAsync(getLocation(), 32);

            if (nearby) {
                Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> spawnIn());
                // Spawn some monsters in.
//                if (getTimerID() == -1) //Confirm we're not already spawning monsters.
//                    setTimerID(Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), this::spawnIn, 35L).getTaskId());
            } else {
                //Noone nearby? Still need to tick timer.
                checkSpawnTimer();
            }
//    		else if (getTimerID() != -1) {
//    			// If no players are around
//    			Bukkit.getScheduler().cancelTask(getTimerID());
//    			setFirstSpawn(true); //The next player to come by should have some monsters spawn.
//    			setTimerID(-1);
//    		}
        }, 0L, 20);
    }

    /*
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
//                    System.out.println("Mob Time: " + newTime);
                    respawnTimes.put(e, newTime);
                } else {
                    respawnTimes.remove(e);
                }
            }

            if (respawnTimes.isEmpty()) return true;
            System.out.println("Cant spawn someone has data!");
            return false;
        }

        System.out.println("Can Spawn!");
        return super.canSpawnMobs();
    }*/
}
