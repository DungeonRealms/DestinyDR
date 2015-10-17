package net.dungeonrealms.spawning;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.types.monsters.BasicEntityBlaze;
import net.dungeonrealms.entities.types.monsters.BasicEntitySkeleton;
import net.dungeonrealms.entities.types.monsters.BasicMageMonster;
import net.dungeonrealms.entities.types.monsters.BasicMeleeMonster;
import net.dungeonrealms.entities.types.monsters.EntityBandit;
import net.dungeonrealms.entities.types.monsters.EntityFireImp;
import net.dungeonrealms.entities.types.monsters.EntityGolem;
import net.dungeonrealms.entities.types.monsters.EntityPirate;
import net.dungeonrealms.entities.types.monsters.EntityRangedPirate;
import net.dungeonrealms.entities.types.monsters.EntitySpider;
import net.dungeonrealms.entities.types.monsters.EntityWitherSkeleton;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.enums.EnumMonster;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.World;

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
    public MobSpawner(Location location, String type, int tier) {
        this.loc = location;
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
        ArmorStand armorStandBase = (ArmorStand) armorstand.getBukkitEntity();
        armorStandBase.setMarker(true);
        armorstand.setGravity(false);
        armorstand.setSmall(true);
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
                    	double num = monster.getBukkitEntity().getLocation().distance(loc);
                        if (num > 32) {
                            monster.setPosition(loc.getX() + 2, loc.getY(), loc.getZ() + 2);
                        }
                    } else {
                        SPAWNED_MONSTERS.remove(monster);
                    }
                }
            }
            if(SPAWNED_MONSTERS.size() < 4) {
                Location location = new Location(Bukkit.getWorlds().get(0), loc.getBlockX() + new Random().nextInt(10), loc.getBlockY(), loc.getBlockZ() + new Random().nextInt(10));
                int attempts = 0;
                while(location.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ()).getType() != Material.AIR){
                	if(attempts >=3){
                		location = loc;
                	}else{	
                		attempts++;
                		location = new Location(Bukkit.getWorlds().get(0), loc.getBlockX() + new Random().nextInt(10), loc.getBlockY(), loc.getBlockZ() + new Random().nextInt(10));
                	}
                }
               Entity entity = null;
               String mob = spawnType;
               World world = armorstand.getWorld();
               EnumEntityType type = EnumEntityType.HOSTILE_MOB;
               if(!mob.contains("*")){
               switch (mob) {
                   case "bandit":
                       entity = new EntityBandit(world, tier, type);
                       break;
                   case "rangedpirate":
                       entity = new EntityRangedPirate(world, type, tier);
                       break;
                   case "pirate":
                       entity = new EntityPirate(world, type, tier);
                       break;
                   case "imp":
                       entity = new EntityFireImp(world, tier, type);
                       break;
                   case "troll":
                       entity = new BasicMeleeMonster(world, EnumMonster.Troll, tier);
                       break;
                   case "goblin":
                       entity = new BasicMeleeMonster(world, EnumMonster.Goblin, tier);
                       break;
                   case "mage":
                       entity = new BasicMageMonster(world, EnumMonster.Mage, tier);
                       break;
                   case "spider":
                       entity = new EntitySpider(world, EnumMonster.Spider, tier);
                       break;
                   case "golem":
                       entity = new EntityGolem(world, tier, type);
                       break;
                   case "naga":
                       entity = new BasicMageMonster(world, EnumMonster.Naga, tier);
                       break;
                   case "wither":
                       // TODO Wither.
                       entity = new EntityWitherSkeleton(world, null, tier);
                       break;
                   case "tripoli":
                       entity = new BasicMeleeMonster(world, EnumMonster.Tripoli, tier);
                       break;
                   case "blaze":
                       entity = new BasicEntityBlaze(world, EnumMonster.Blaze, tier);
                       break;
                   case "skeleton":
                	   entity = new BasicEntitySkeleton(world, tier);
                	   break;
                   default:
                	   Utils.log.info(mob + " is not created yet.");
                       entity = new EntityBandit(world, tier, type);
               }
               }else{
            	   //Elite Mob
            	   mob = mob.replace("*", "");
            	   
                   switch (mob) {
                   case "bandit":
                       entity = new EntityBandit(world, tier, type);
                       break;
                   case "rangedpirate":
                       entity = new EntityRangedPirate(world, type, tier);
                       break;
                   case "pirate":
                       entity = new EntityPirate(world, type, tier);
                       break;
                   case "imp":
                       entity = new EntityFireImp(world, tier, type);
                       break;
                   case "troll":
                       entity = new BasicMeleeMonster(world, EnumMonster.Troll, tier);
                       break;
                   case "goblin":
                       entity = new BasicMeleeMonster(world, EnumMonster.Goblin, tier);
                       break;
                   case "mage":
                       entity = new BasicMageMonster(world, EnumMonster.Mage, tier);
                       break;
                   case "spider":
                       entity = new EntitySpider(world, EnumMonster.Spider, tier);
                       break;
                   case "golem":
                       entity = new EntityGolem(world, tier, type);
                       break;
                   case "naga":
                       entity = new BasicMageMonster(world, EnumMonster.Naga, tier);
                       break;
                   case "wither":
                       // TODO Wither.
                       entity = new EntityWitherSkeleton(world, null, tier);
                       break;
                   case "tripoli":
                       entity = new BasicMeleeMonster(world, EnumMonster.Tripoli, tier);
                       break;
                   case "blaze":
                       entity = new BasicEntityBlaze(world, EnumMonster.Blaze, tier);
                       break;
                   default:
                       entity = new EntityBandit(world, tier, type);
               }
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
            armorstand.getWorld().kill(spawnedMonster);
        }
    }

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
			Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), ()->{
				spawnIn();
			}, 0, 4 * 20);
		}else	{
			kill();
		}
	}
}
