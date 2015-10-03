package net.dungeonrealms.spawning;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.NMSUtils;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chase on Sep 28, 2015
 */
public class SpawningMechanics {
    private static ArrayList<MobSpawner> spawners = new ArrayList<>();

    public static void updateSpawners() {
        if (spawners.size() > 0)
            spawners.stream().filter(MobSpawner::playersAround).forEach(net.dungeonrealms.spawning.MobSpawner::spawnIn);
    }

    public static ArrayList<MobSpawner> getSpawners() {
        return spawners;
    }

    public static void add(MobSpawner spawner) {
        spawners.add(spawner);
    }

    public static void loadSpawners() {
        List<Entity> list = Bukkit.getWorlds().get(0).getEntities();
        for (Entity aList : list) {
            if (aList instanceof EntityArmorStand || aList instanceof ArmorStand) {
                net.minecraft.server.v1_8_R3.Entity nms = NMSUtils.getNMSEntity(aList);
                if (nms.getNBTTag() != null && nms.getNBTTag().hasKey("type")) {
                    if (nms.getNBTTag().getString("type").equalsIgnoreCase("spawner")) {
                        spawners.add(new MobSpawner((EntityArmorStand) nms));
                    }
                }
            } else {
            }
        }
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), SpawningMechanics::updateSpawners,
                0, 10 * 20L);
    }

    /**
     * @param i
     */
    public static void remove(int i) {
        spawners.remove(i);
    }
}
