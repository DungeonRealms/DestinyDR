package net.dungeonrealms.spawning;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.EnumMonster;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Chase on Sep 25, 2015
 */
public class MobSpawner {
    public Location loc;
    public String spawnType;
    public EntityArmorStand armorstand;
    public int tier;
    public List<Entity> SPAWNED_MONSTERS = new CopyOnWriteArrayList<>();
    public boolean isElite = false;
    public int spawnAmount;
    public int id;
    public int timerID;
    
    public MobSpawner(Location location, String type, int tier, int spawnAmount, int configid) {
    	this.spawnAmount = spawnAmount;
        this.loc = location;
        this.id = configid;
        this.spawnType = type;
        if(type.contains("*"))
        	isElite = true;
        this.tier = tier;
        World world = ((CraftWorld) location.getWorld()).getHandle();
        armorstand = new EntityArmorStand(world);
        armorstand.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "spawner"));
        armorstand.getBukkitEntity().setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), tier));
        armorstand.getBukkitEntity().setMetadata("monsters", new FixedMetadataValue(DungeonRealms.getInstance(), type));
//      armorstand.setInvisible(true);
        List<org.bukkit.entity.Entity> list = armorstand.getBukkitEntity().getNearbyEntities(loc.getX(), loc.getY(), loc.getZ());
        if(list.size() > 0){
        	for(org.bukkit.entity.Entity entity : list){
        		if(entity instanceof ArmorStand){
        			entity.remove();
//        			armorstand.getWorld().removeEntity(((CraftMonster)entity).getHandle());
        			((ArmorStand) entity).setHealth(0);
        			if(armorstand.getBukkitEntity().getWorld().getBlockAt(loc).getType() == Material.ARMOR_STAND)
        			armorstand.getBukkitEntity().getWorld().getBlockAt(loc).setType(Material.AIR);
        		}
        	}
        }
        armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
        world.addEntity(armorstand, SpawnReason.CUSTOM);
        armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
//        ArmorStand armorStandBase = (ArmorStand) armorstand.getBukkitEntity();
//        armorStandBase.setMarker(true);
//        armorstand.setGravity(false);
//        armorstand.setSmall(true);
    }

    /**
     * Does 1 rotation of spawning for this mob spawner.
     */
    public void spawnIn() {
    	if(isElite){
    		
    	}else{
    	if(loc.getChunk().isLoaded()){
            if (!SPAWNED_MONSTERS.isEmpty()) {
                for (Entity monster : SPAWNED_MONSTERS) {
                    if (monster.isAlive()) {
                    	if(API.isInSafeRegion(monster.getBukkitEntity().getLocation())){
                            monster.setPosition(loc.getX() + 2, loc.getY(), loc.getZ() + 2);
                    	}
                    	double num = monster.getBukkitEntity().getLocation().distance(loc);
                        if (num > 32) {
                            monster.setPosition(loc.getX() + 2, loc.getY(), loc.getZ() + 2);
                        }
                    } else {
                        SPAWNED_MONSTERS.remove(monster);
                    }
                }
            }
            if(SPAWNED_MONSTERS.size() < spawnAmount * 2) {
                Location location = new Location(Bukkit.getWorlds().get(0), loc.getBlockX() + new Random().nextInt(10), loc.getBlockY(), loc.getBlockZ() + new Random().nextInt(10));
//                int attempts = 0;
//                while(location.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ()).getType() != Material.AIR){
//                	if(attempts >=3){
//                		location = loc;
//                	}else{	
//                		attempts++;
//                		location = new Location(Bukkit.getWorlds().get(0), loc.getBlockX() + new Random().nextInt(10), loc.getBlockY(), loc.getBlockZ() + new Random().nextInt(10));
//                	}
//                }
               String mob = spawnType;
               World world = armorstand.getWorld();
               EnumEntityType type = EnumEntityType.HOSTILE_MOB;
               EnumMonster monsEnum = EnumMonster.getMonsterByString(mob);
               if(monsEnum == null)
            	   return;
               if(mob.contains("*")){
            	   mob = mob.replace("*", "");
            	   isElite = true;
               }
               Entity entity = SpawningMechanics.getMob(world, tier, monsEnum);
               if(isElite){
            	   int lvl = Utils.getRandomFromTier(tier);
            	   EntityStats.setMonsterElite(entity, lvl, tier);
               }
               entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
               world.addEntity(entity, SpawnReason.CUSTOM);
               entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
               SPAWNED_MONSTERS.add(entity);
           }
    	}else{
    	    if (!SPAWNED_MONSTERS.isEmpty()){
                for (Entity monster : SPAWNED_MONSTERS) {
                    monster.die();
                    monster.dead = true;
                    monster.getBukkitEntity().remove();
                    armorstand.getWorld().kill(monster);
                    SPAWNED_MONSTERS.remove(monster);
                }
            }
    	}
    	}
    }

    /**
     * Kill all spawnedMonsters for this Mob Spawner
     */
    public void kill() {
        for (Entity spawnedMonster : SPAWNED_MONSTERS) {
            spawnedMonster.getBukkitEntity().remove();
            spawnedMonster.damageEntity(DamageSource.GENERIC, 20f);
            spawnedMonster.dead = true;
            armorstand.getWorld().kill(spawnedMonster);
        }
    }
    
    public void remove(){
    	kill();
    	armorstand.getWorld().removeEntity(armorstand);
    	armorstand.getBukkitEntity().remove();
    	SpawningMechanics.SPAWNER_CONFIG.set(id, null);
		DungeonRealms.getInstance().getConfig().set("spawners", SpawningMechanics.SPAWNER_CONFIG);
		DungeonRealms.getInstance().saveConfig();
    	isRemoved = true;
    }

    public boolean isRemoved = false;
    
	/**
	 * @return
	 */
	public List<Entity> getSpawnedMonsters() {
		return SPAWNED_MONSTERS;
	}

	/**
	 * @return
	 */
	public boolean isSpawning() {
		return !API.getNearbyPlayers(loc, 100).isEmpty();
	}

	/**
	 * Initialize spawner
	 */
	public void init() {
		if(isSpawning()){
			timerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), ()-> {
			if(isRemoved){
				Bukkit.getScheduler().cancelTask(timerID);
			} else
			    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), this::spawnIn, 0L);
			}, 0, 80L);
		}else	{
			Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), this::kill, 5L);
		}
	}
}
