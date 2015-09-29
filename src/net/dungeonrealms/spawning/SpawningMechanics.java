package net.dungeonrealms.spawning;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.NMSUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.EntityArmorStand;

/**
 * Created by Chase on Sep 28, 2015
 */
public class SpawningMechanics {
	private static ArrayList<MobSpawner> spawners = new ArrayList<>();

	public static void updateSpawners() {
		for (int i = 0; i < spawners.size(); i++) {
			MobSpawner current = spawners.get(i);
			if (current.playersAround()) {
			if (!current.isSpawning) {
				current.isSpawning = true;
				current.spawnIn();
				current.doSpawn();
			}
			}
		}
	}

	public static void add(MobSpawner spawner) {
		spawners.add(spawner);
	}

	public static void loadSpawners() {
		Bukkit.broadcastMessage("spawners loading");
		List<Entity> list = Bukkit.getWorlds().get(0).getEntities();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) instanceof EntityArmorStand || list.get(i) instanceof ArmorStand) {
			net.minecraft.server.v1_8_R3.Entity nms = NMSUtils.getNMSEntity(list.get(i));
			if (nms.getNBTTag() != null && nms.getNBTTag().hasKey("type")) {
				if (nms.getNBTTag().getString("type").equalsIgnoreCase("spawner")) {
					spawners.add(new MobSpawner((EntityArmorStand) nms));
				}
			}
			} else {
			Utils.log.info(list.get(i).getClass().getSimpleName());
			}
		}
		Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> updateSpawners(), 20l,
			20 * 20l);
	}
}
