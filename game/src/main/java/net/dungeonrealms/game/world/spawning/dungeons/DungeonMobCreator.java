package net.dungeonrealms.game.world.spawning.dungeons;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.InfernalLordsGuard;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.spawning.EliteMobSpawner;
import net.dungeonrealms.game.world.spawning.MobSpawner;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityInsentient;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DungeonMobCreator - Handles creation of dungeon mobs from config files.
 * 
 * Redone on April 23rd, 2017.
 * @author Kneesnap
 */
public class DungeonMobCreator {

    public static ConcurrentHashMap<Entity, Location> getEntitiesToSpawn(String instanceName, World world) {
        ConcurrentHashMap<Entity, Location> toSpawn = new ConcurrentHashMap<>();
        Map<Location, String> spawnData = DungeonManager.instance_mob_spawns.get(instanceName);
        net.minecraft.server.v1_9_R2.World craftWorld = ((CraftWorld) world).getHandle();

        DungeonManager.DungeonObject object = DungeonManager.getInstance().getDungeon(world);
        
        for (Location loc : spawnData.keySet()) {
        	MobSpawner spawner = SpawningMechanics.loadSpawner(spawnData.get(loc));
        	
        	for (int i = 0; i < spawner.getSpawnAmount(); i++) {
        		Location spawn = spawner.spray();
        		
        		if (!spawn.getChunk().isLoaded())
        			spawn.getChunk().load();
        		
        		int level = Utils.getRandomFromTier(spawner.getTier(), spawner.getLvlRange());
        		
        		Entity entity;
        		if (spawner instanceof EliteMobSpawner) {
            		entity = EntityAPI.createElite(loc, ((EliteMobSpawner)spawner).getEliteType(), spawner.getMonsterType(), spawner.getTier(), level, spawner.getCustomName(), true);
            	} else {
            		entity = EntityAPI.createCustomMonster(loc, spawner.getMonsterType(), level, spawner.getTier(), spawner.getWeaponType(), spawner.getCustomName());
            		//TODO: Run makeDungeonMob when this spawns.
            	}
        		
        		((LivingEntity)entity.getBukkitEntity()).setRemoveWhenFarAway(false);
                ((EntityInsentient) entity).persistent = true;
                toSpawn.put(entity, loc);
        	}
        }
    }
}
