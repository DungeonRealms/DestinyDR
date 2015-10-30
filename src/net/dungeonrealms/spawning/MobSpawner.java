package net.dungeonrealms.spawning;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.EnumMonster;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.DamageSource;
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
    public int spawnAmount;
    public int id;
    public int timerID;
    public String lvlRange;
    public String eliteName;
    
    public MobSpawner(Location location, String type, int tier, int spawnAmount, int configid, String lvlRange) {
		if (type.contains("(")) 
        	isElite = true;
        if(isElite){
			 eliteName = type.substring(type.indexOf("(") + 1,
					type.indexOf(")"));
			eliteName = eliteName.replace("_", " ");
			
			type = type.substring(0, type.indexOf("("));
			if(type.contains("*"))
				type = type.replace("*", "");
			
			spawnAmount = 1;
        }
        this.lvlRange = lvlRange;
    	this.spawnAmount = spawnAmount;
        this.loc = location;
        this.id = configid;
        this.spawnType = type;
        this.tier = tier;
        World world = ((CraftWorld) location.getWorld()).getHandle();
        armorstand = new EntityArmorStand(world);
        armorstand.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "spawner"));
        armorstand.getBukkitEntity().setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), tier));
        armorstand.getBukkitEntity().setMetadata("monsters", new FixedMetadataValue(DungeonRealms.getInstance(), type));
        List<org.bukkit.entity.Entity> list = armorstand.getBukkitEntity().getNearbyEntities(loc.getX(), loc.getY(), loc.getZ());
        if(list.size() > 0){
        	for(org.bukkit.entity.Entity entity : list){
        		if(entity instanceof ArmorStand){
        			entity.remove();
        			((ArmorStand) entity).setHealth(0);
        			if(armorstand.getBukkitEntity().getWorld().getBlockAt(loc).getType() == Material.ARMOR_STAND)
        			armorstand.getBukkitEntity().getWorld().getBlockAt(loc).setType(Material.AIR);
        		}
        	}
        }
        armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
        world.addEntity(armorstand, SpawnReason.CUSTOM);
        armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());	
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
            if(spawnType.contains("*")){
            	spawnType = spawnType.replace("*", "");
         	   isElite = true;
            }
            if(isElite){
            	if(SPAWNED_MONSTERS.size() == 0){
            		Utils.log.info("Elite set to spawn");
            		Location location = new Location(Bukkit.getWorlds().get(0), loc.getBlockX() + new Random().nextInt(10), loc.getBlockY(), loc.getBlockZ() + new Random().nextInt(10));
                    if(location.getBlock().getType() != Material.AIR || location.add(0, 1, 0).getBlock().getType() != Material.AIR)
                    	return;
                	String mob = spawnType;
                	World world = armorstand.getWorld();
                	EnumMonster monsEnum = EnumMonster.getMonsterByString(mob);
                	if(monsEnum == null)
             	   		return;
                	Entity entity = SpawningMechanics.getMob(world, tier, monsEnum);
                    int level = Utils.getRandomFromTier(tier, lvlRange);
                    MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, tier, level);
                    EntityStats.setMonsterRandomStats(entity, level, tier);
                	
                	if(entity == null)
                		return;
                	
                    String lvl = ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] "+ChatColor.RESET;
                    String healthName = entity.getBukkitEntity().getMetadata("currentHP").get(0).asInt()+ChatColor.RED.toString() + "❤";
                    String customName = entity.getBukkitEntity().getMetadata("customname").get(0).asString();
                    ArmorStand stand = entity.getBukkitEntity().getLocation().getWorld().spawn(entity.getBukkitEntity().getLocation(), ArmorStand.class);
                    stand.setRemoveWhenFarAway(false);
                    stand.setVisible(false);
                    stand.setSmall(true);
                    stand.setBasePlate(false);
                    stand.setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "nametag"));
                    stand.setGravity(false);
                    stand.setArms(false);
                    stand.setCustomNameVisible(true);
                    stand.setCustomName(lvl + customName + healthName);
                    LivingEntity ent = stand;
                    ent.setRemoveWhenFarAway(false);
                    entity.getBukkitEntity().setPassenger(stand);
             		EntityStats.setMonsterElite(entity, level, tier);
             		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
             			Utils.log.info("Elite spawned");
             			entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
             			world.addEntity(entity, SpawnReason.CUSTOM);
                		entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
             		}, (20 * 60) * 5);
                	SPAWNED_MONSTERS.add(entity);
            	}

            }else{
            if(SPAWNED_MONSTERS.size() < spawnAmount * 2) {
               Location location = new Location(Bukkit.getWorlds().get(0), loc.getBlockX() + new Random().nextInt(10), loc.getBlockY(), loc.getBlockZ() + new Random().nextInt(10));
               if(location.getBlock().getType() != Material.AIR || location.add(0, 1, 0).getBlock().getType() != Material.AIR)
                	return;
               String mob = spawnType;
               World world = armorstand.getWorld();
               EnumEntityType type = EnumEntityType.HOSTILE_MOB;
               EnumMonster monsEnum = EnumMonster.getMonsterByString(mob);
               if(monsEnum == null)
            	   return;
               		Entity entity = SpawningMechanics.getMob(world, tier, monsEnum);
               		
                    int level = Utils.getRandomFromTier(tier, lvlRange);
                    MetadataUtils.registerEntityMetadata(entity, type, tier, level);
                    EntityStats.setMonsterRandomStats(entity, level, tier);
                	
                    String lvl = ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] "+ChatColor.RESET;
                    String healthName = entity.getBukkitEntity().getMetadata("currentHP").get(0).asInt()+ChatColor.RED.toString() + "❤";
                    String customName = entity.getBukkitEntity().getMetadata("customname").get(0).asString();
                    ArmorStand stand = entity.getBukkitEntity().getLocation().getWorld().spawn(entity.getBukkitEntity().getLocation(), ArmorStand.class);
                    stand.setRemoveWhenFarAway(false);
                    stand.setVisible(false);
                    stand.setSmall(true);
                    stand.setBasePlate(false);
                    stand.setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "nametag"));
                    stand.setGravity(false);
                    stand.setArms(false);
                    stand.setCustomNameVisible(true);
                    stand.setCustomName(lvl + customName + healthName);
                    LivingEntity ent = stand;
                    ent.setRemoveWhenFarAway(false);
                    entity.getBukkitEntity().setPassenger(stand);
                    
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
               		entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
               		world.addEntity(entity, SpawnReason.CUSTOM);
               		entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
               		SPAWNED_MONSTERS.add(entity);
                    },20 * 10);
            	}
            }
    	}else{
    	    if (!SPAWNED_MONSTERS.isEmpty()){
                for (Entity monster : SPAWNED_MONSTERS) {
                	monster.passenger.die();
                	monster.passenger.dead = true;
                	if(monster.getBukkitEntity().getPassenger() != null && monster.getBukkitEntity().getPassenger().hasMetadata("type")){
                		monster.getBukkitEntity().getPassenger().remove();
                		((CraftEntity)monster.getBukkitEntity().getPassenger()).getHandle().die();

                	}
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
            if(spawnedMonster.getBukkitEntity().getPassenger() != null){
            	spawnedMonster.getBukkitEntity().getPassenger().remove();
            }
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
