package net.dungeonrealms.spawning;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.types.monsters.*;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.enums.EnumMonster;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
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
	public boolean isSpawning;

	public MobSpawner(Location location, String type, int tier) {
		this.loc = location;
		this.spawnType = type;
		this.tier = tier;
		isSpawning = false;
		World world = ((CraftWorld) location.getWorld()).getHandle();
		armorstand = new EntityArmorStand(world);
		armorstand.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "spawner"));
		armorstand.getBukkitEntity().setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), tier));
		armorstand.getBukkitEntity().setMetadata("monsters", new FixedMetadataValue(DungeonRealms.getInstance(), type));
		armorstand.setInvisible(true);
		armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
		world.addEntity(armorstand, SpawnReason.CUSTOM);
		armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
	}

	public boolean playersAround() {
		List<Player> players = API.getNearbyPlayers(loc, 20);
		return !(players == null || players.size() <= 0);
	}

	public List getSpawnedMonsters() {
		return this.SPAWNED_MONSTERS;
	}

	/**
	 * Does 1 rotation of spawning for this mob spawner.
	 */
	public void spawnIn() {
		for (int i = 0; i < SPAWNED_MONSTERS.size(); i++) {
			if (!SPAWNED_MONSTERS.get(i).isAlive())
				SPAWNED_MONSTERS.remove(i);
			if (SPAWNED_MONSTERS.get(i).getBukkitEntity().getLocation().distance(loc) >= 30)
				SPAWNED_MONSTERS.get(i).setPosition(loc.getX() + 2, loc.getY(), loc.getZ() + 2);
		}
		if (isSpawning) {
			if (!playersAround()) {
				if (SPAWNED_MONSTERS.size() > 0)
					for (Entity ent : SPAWNED_MONSTERS) {
						ent.die();
						ent.dead = true;
						ent.getBukkitEntity().remove();
						armorstand.getWorld().kill(ent);
						SPAWNED_MONSTERS.remove(ent);
					}
				isSpawning = false;
				return;
			}
		} else {
			if (!playersAround()) {
				return;
			} else {
				isSpawning = true;
			}
		}
		// Max spawn ammount
		if (SPAWNED_MONSTERS.size() > 4) {
			Bukkit.broadcastMessage("max spawn reached for spawner");
			return;
		}
		for (int i = 0; i < 4 - SPAWNED_MONSTERS.size(); i++) {
			Location location = new Location(Bukkit.getWorlds().get(0), loc.getX() + new Random().nextInt(3),
			        loc.getY(), loc.getZ() + new Random().nextInt(3));
			if (!location.getChunk().isLoaded())
				return;
			Entity entity;
			String mob = spawnType;
			World world = armorstand.getWorld();
			EnumEntityType type = EnumEntityType.HOSTILE_MOB;
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
			entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
			world.addEntity(entity, SpawnReason.CUSTOM);
			entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
			SPAWNED_MONSTERS.add(entity);
		}

	}

	/**
	 * Kill all spawnedMonsters for this Mob Spawner
	 */
	public void kill() {
		for (Entity spawnedMonster : SPAWNED_MONSTERS) {
			spawnedMonster.getBukkitEntity().remove();
			armorstand.getWorld().kill(spawnedMonster);
			armorstand.getWorld().kill(armorstand);
			armorstand.setHealth(0);
			SpawningMechanics.remove(this);
		}
	}
}
