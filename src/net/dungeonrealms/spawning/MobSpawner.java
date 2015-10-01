package net.dungeonrealms.spawning;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.types.monsters.EntityBandit;
import net.dungeonrealms.entities.types.monsters.EntityFireImp;
import net.dungeonrealms.entities.types.monsters.EntityPirate;
import net.dungeonrealms.entities.types.monsters.EntityRangedPirate;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Chase on Sep 25, 2015
 */
public class MobSpawner {
	public Location loc;
	public String[] spawnType;
	public EntityArmorStand armorstand;
	public int tier;
	public ArrayList<Entity> spawnedMonsters = new ArrayList<>();
	public boolean isSpawning;

	public MobSpawner(Location location, String[] type, int tier) {
		this.loc = location;
		this.spawnType = type;
		this.tier = tier;
		isSpawning = false;
		World world = ((CraftWorld) location.getWorld()).getHandle();
		armorstand = new EntityArmorStand(world);
		armorstand.getBukkitEntity().setMetadata("type",
			new FixedMetadataValue(DungeonRealms.getInstance(), "spawner"));
		String temp = "";
		for (String aType : type) {
			temp += aType + ",";
		}
		armorstand.getBukkitEntity().setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), tier));
		armorstand.getBukkitEntity().setMetadata("monsters", new FixedMetadataValue(DungeonRealms.getInstance(), temp));
		armorstand.setInvisible(false);
		armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
		world.addEntity(armorstand, SpawnReason.CUSTOM);
		armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
	}

	public MobSpawner(EntityArmorStand stand) {
		this.loc = new Location(Bukkit.getWorlds().get(0), stand.locX, stand.locY, stand.locZ);
		String monsters = stand.getBukkitEntity().getMetadata("monsters").get(0).asString();
		this.spawnType = monsters.split(",");
		this.tier = stand.getBukkitEntity().getMetadata("tier").get(0).asInt();
		isSpawning = false;
		armorstand = stand;
		armorstand.setInvisible(false);
	}

	public boolean playersAround() {
		List<Player> players = API.getNearbyPlayers(loc, 20);
		return !(players == null || players.size() <= 0);
	}

	public void spawnIn() {
		for (int i = 0; i < spawnedMonsters.size(); i++) {
			if (!spawnedMonsters.get(i).isAlive())
			spawnedMonsters.remove(i);
		}
		if (isSpawning) {
			if (!playersAround()) {
			if (spawnedMonsters.size() > 0)
				for (Entity ent : spawnedMonsters) {
					ent.die();
					ent.dead = true;
					ent.getBukkitEntity().remove();
					armorstand.getWorld().kill(ent);
					spawnedMonsters.remove(ent);
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
		if (spawnedMonsters.size() > 4) {
			Bukkit.broadcastMessage("max spawn reached for spawner");
			return;
		}
		for (int i = 0; i < 4 - spawnedMonsters.size(); i++) {
			Entity entity = null;
			String mob = spawnType[new Random().nextInt(spawnType.length)];
			Utils.log.info(mob);
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
			}
			Location location = new Location(Bukkit.getWorlds().get(0), loc.getX() + new Random().nextInt(3),
				loc.getY(), loc.getZ() + new Random().nextInt(3));
			if (entity != null) {
				entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
				world.addEntity(entity, SpawnReason.CUSTOM);
				entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
			}
			spawnedMonsters.add(entity);
		}

	}

}
