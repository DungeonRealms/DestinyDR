package net.dungeonrealms.spawning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.types.monsters.EntityBandit;
import net.dungeonrealms.entities.types.monsters.EntityFireImp;
import net.dungeonrealms.entities.types.monsters.EntityPirate;
import net.dungeonrealms.entities.types.monsters.EntityRangedPirate;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mechanics.XRandom;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.World;

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
			temp += type[1] + ",";
		}
		armorstand.getBukkitEntity().setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), tier));
		armorstand.getBukkitEntity().setMetadata("monsters", new FixedMetadataValue(DungeonRealms.getInstance(), temp));
		armorstand.setInvisible(false);
		armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
		// armorstand.spawnIn(world);
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
		Bukkit.broadcastMessage("Loaded a spawner");
	}

	public boolean playersAround() {
		List<Player> players = API.getNearbyPlayers(loc, 20);
		Bukkit.broadcastMessage(players.size() + " ");
		if (players == null || players.size() <= 0)
			return false;
		else
			return true;
	}

	private HashMap<Entity, Location> toSpawn = new HashMap<>();

	public void spawnIn() {
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
			for (int i = 0; i < spawnedMonsters.size(); i++) {
				if (spawnedMonsters.get(i).dead)
					spawnedMonsters.remove(i);
			}
			if (!playersAround() || !isSpawning) {
				Bukkit.broadcastMessage("timer cancelled");
				Bukkit.broadcastMessage(playersAround() + "");
				Bukkit.broadcastMessage(isSpawning + "");
				isSpawning = false;
				this.cancel();
			}

			// Max spawn ammount
			if (spawnedMonsters.size() > 4) {
				Bukkit.broadcastMessage("max spawn reached for spawner");
				return;
			}
			Entity entity = null;
			String mob = spawnType[new XRandom().nextInt(spawnType.length - 1)];
			World world = armorstand.getWorld();
			EnumEntityType type = EnumEntityType.HOSTILE_MOB;
			switch (mob) {
			case "bandit":
				entity = new EntityBandit(world, tier, type);
			case "rangedpirate":
				entity = new EntityRangedPirate(world, type, tier);
			case "pirate":
				entity = new EntityPirate(world, type, tier);
			case "imp":
				entity = new EntityFireImp(world, tier, type);
			}
			entity.setLocation(loc.getX() + new XRandom().nextInt(10), loc.getY(),
					loc.getZ() + new XRandom().nextInt(10), 1, 1);
			// world.addEntity(entity, SpawnReason.CUSTOM);
			toSpawn.put(entity, new Location(Bukkit.getWorlds().get(0), loc.getX() + new XRandom().nextInt(10),
					loc.getY(), loc.getZ() + new XRandom().nextInt(10), 1, 1));
			spawnedMonsters.add(entity);
			Bukkit.broadcastMessage("Spawned " + mob + " in at " + loc.toString());
			}

		}, 0L, 10 * 1000);
	}

	public void doSpawn() {
		while (toSpawn.size() > 0) {
			Entity ent = (Entity) toSpawn.keySet().toArray()[0];
			Location loc = (Location) toSpawn.values().toArray()[0];
			ent.setLocation(loc.getX(), loc.getY(), loc.getZ(), 1, 1);
			armorstand.getWorld().addEntity(ent, SpawnReason.CUSTOM);
			ent.setLocation(loc.getX(), loc.getY(), loc.getZ(), 1, 1);
			toSpawn.remove(ent);
		}
	}
}
