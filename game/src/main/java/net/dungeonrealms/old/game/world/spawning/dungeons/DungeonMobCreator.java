package net.dungeonrealms.old.game.world.spawning.dungeons;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.old.game.mastery.Utils;
import net.dungeonrealms.old.game.mechanic.DungeonManager;
import net.dungeonrealms.old.game.world.entity.EnumEntityType;
import net.dungeonrealms.old.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.old.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.old.game.world.entity.util.EntityStats;
import net.dungeonrealms.old.game.world.spawning.SpawningMechanics;
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
 * Created by Kieran Quigley (Proxying) on 15-Jun-16.
 */
public class DungeonMobCreator {

    public static ConcurrentHashMap<Entity, Location> getEntitiesToSpawn(String instanceName, World world) {
        ConcurrentHashMap<Entity, Location> toSpawn = new ConcurrentHashMap<>();
        Map<Location, String> spawnData = DungeonManager.instance_mob_spawns.get(instanceName);
        net.minecraft.server.v1_9_R2.World craftWorld = ((CraftWorld) world).getHandle();
        for (Map.Entry<Location, String> entry : spawnData.entrySet()) {
            int tier;
            boolean hasCustomName = false;
            boolean isElite = false;
            String customName = "";
            Location location = entry.getKey();
            String data = entry.getValue();
            String tierString = data.substring(data.indexOf(":"), data.indexOf(";"));
            tierString = tierString.substring(1);
            tier = Integer.parseInt(tierString);
            Character amountString = data.charAt(data.indexOf(";") + 1);
            int spawnAmount = Integer.parseInt(String.valueOf(amountString));
            String monsterType;
            String spawnRange = String.valueOf(data.charAt(data.lastIndexOf("@") - 1));
            String[] locationRange = data.substring(data.indexOf("#") + 1, data.lastIndexOf("$")).split("-");
            int minXZ = Integer.parseInt(locationRange[0]);
            int maxXZ = Integer.parseInt(locationRange[1]);
            if (data.contains("*")) {
                data = data.replace("*", "");
                isElite = true;
            }
            if (data.contains("(")) {
                hasCustomName = true;
                customName = data.substring(data.indexOf("(") + 1, data.indexOf(")"));
                customName = customName.replaceAll("_", " ");
                monsterType = data.substring(0, data.indexOf("("));
            } else {
                monsterType = data.split(":")[0];
            }
            EnumEntityType enumEntityType = EnumEntityType.HOSTILE_MOB;
            EnumMonster enumMonster;
            if (hasCustomName) {
                if (customName.toLowerCase().contains("undead")) {
                    String spawnTypeLower = monsterType.toLowerCase();
                    if (!spawnTypeLower.equals("skeleton") && !spawnTypeLower.equals("skeleton1") && !spawnTypeLower.equals("skeleton2")) {
                        monsterType = "undead";
                    }
                } else if (customName.toLowerCase().contains("mountain")) {
                    monsterType = "frozenskeleton";
                } else if (customName.toLowerCase().contains("bandit pyromancer")) {
                    tier = 1;
                }
            }
            enumMonster = EnumMonster.getMonsterByString(monsterType);
            for (int i = 0; i < spawnAmount; i++) {
                Location toSpawnLocation = getRandomLocation(world, location, ((location.getX() - minXZ) - maxXZ), ((location.getX() + minXZ) + maxXZ), ((location.getZ() - minXZ) - maxXZ), ((location.getZ() + minXZ) + maxXZ));
                if (toSpawnLocation.getBlock().getType() != Material.AIR) {
                    if (toSpawnLocation.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                        toSpawnLocation.add(0, 1, 0);
                    }
                }
                if (!toSpawnLocation.getChunk().isLoaded()) {
                    toSpawnLocation.getChunk().load();
                }
                if (enumMonster == null) {
                    continue;
                }
                Entity entity = SpawningMechanics.getMob(craftWorld, tier, enumMonster);
                if (entity == null) {
                    continue;
                }
                int level = Utils.getRandomFromTier(tier + 1, spawnRange);
                entity.getBukkitEntity().setMetadata("dungeon", new FixedMetadataValue(DungeonRealms.getInstance(), true));
                if (!isElite) {
                    EntityStats.createDungeonMob(entity, level, tier);
                    SpawningMechanics.rollElement(entity, enumMonster);
                    String levelName = ChatColor.AQUA.toString() + "[Lvl. " + level + "] ";
                    if (hasCustomName) {
                        entity.setCustomName(levelName + GameAPI.getTierColor(tier) + customName.trim());
                        entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), GameAPI.getTierColor(tier) + customName.trim()));
                    } else {
                        entity.setCustomName(levelName + GameAPI.getTierColor(tier) + enumMonster.name.trim());
                        entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), GameAPI.getTierColor(tier) + enumMonster.name.trim()));
                    }
                } else {
                    entity.getBukkitEntity().setMetadata("elite", new FixedMetadataValue(DungeonRealms.getInstance(), true));
                    entity.getBukkitEntity().setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), tier));
                    EntityStats.setMonsterElite(entity, EnumNamedElite.NONE, tier, enumMonster, level, true);
                    SpawningMechanics.rollElement(entity, enumMonster);
                    if (hasCustomName) {
                        entity.setCustomName(GameAPI.getTierColor(tier).toString() + ChatColor.BOLD + customName.trim());
                        entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), GameAPI.getTierColor(tier).toString() + ChatColor.BOLD + customName.trim()));
                    } else {
                        entity.setCustomName(GameAPI.getTierColor(tier).toString() + ChatColor.BOLD + enumMonster.name.trim());
                        entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), GameAPI.getTierColor(tier).toString() + ChatColor.BOLD + enumMonster.name.trim()));
                    }
                }
                entity.setLocation(toSpawnLocation.getX(), toSpawnLocation.getY(), toSpawnLocation.getZ(), 1, 1);
                ((LivingEntity)entity.getBukkitEntity()).setRemoveWhenFarAway(false);
                ((EntityInsentient) entity).persistent = true;
                toSpawn.put(entity, toSpawnLocation);
            }
        }
        return toSpawn;
    }

    private static Location getRandomLocation(World world, Location location, double xMin, double xMax, double zMin, double zMax) {
        double randomX;
        double randomZ;

        double x;
        double y;
        double z;

        randomX = xMin + (int) (Math.random() * (xMax - xMin + 1));
        randomZ = zMin + (int) (Math.random() * (zMax - zMin + 1));

        x = randomX;
        y = location.getY();
        z = randomZ;

        x = x + 0.5; // add .5 so they spawn in the middle of the block
        z = z + 0.5;
        y = y + 2.0;

        return new Location(world, x, y, z);
    }
}
