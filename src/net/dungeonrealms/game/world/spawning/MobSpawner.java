package net.dungeonrealms.game.world.spawning;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

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
    public int timerID = -1;
    public String lvlRange;
    public String eliteName;
    boolean firstSpawn = true;
    public boolean toSpawn;

    public MobSpawner(Location location, String type, int tier, int spawnAmount, int configid, String lvlRange) {
        if (type.contains("("))
            isElite = true;
        if (isElite) {
            eliteName = type.substring(type.indexOf("(") + 1, type.indexOf(")"));
            eliteName = eliteName.replace("_", " ");

            type = type.substring(0, type.indexOf("("));
            if (type.contains("*"))
                type = type.replace("*", "");

            spawnAmount = 1;
        }
        if (type.contains("*")) {
            type = type.replace("*", "");
        }
        if (spawnAmount > 5)
            spawnAmount = 5;
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
        List<org.bukkit.entity.Entity> list = armorstand.getBukkitEntity().getNearbyEntities(loc.getX(), loc.getY(),
                loc.getZ());
        if (list.size() > 0) {
            list.stream().filter(entity -> entity instanceof ArmorStand).forEach(entity -> {
                entity.remove();
                ((ArmorStand) entity).setHealth(0);
                if (armorstand.getBukkitEntity().getWorld().getBlockAt(loc).getType() == Material.ARMOR_STAND)
                    armorstand.getBukkitEntity().getWorld().getBlockAt(loc).setType(Material.AIR);
            });
        }
        armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
        world.addEntity(armorstand, SpawnReason.CUSTOM);
        armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Does 1 rotation of spawning for this mob spawner.
     */
    public void spawnIn(boolean not) {
        if (toSpawn) return;
        if (!not) {
            if (!SPAWNED_MONSTERS.isEmpty()) {
                for (Entity monster : SPAWNED_MONSTERS) {
                    if (monster.isAlive()) {
                        if (API.isInSafeRegion(monster.getBukkitEntity().getLocation())) {
                            monster.setPosition(loc.getX(), loc.getY(), loc.getZ());
                        }
                        double num = monster.getBukkitEntity().getLocation().distance(loc);
                        if (num > 32) {
                            monster.setPosition(loc.getX() + 2, loc.getY(), loc.getZ() + 2);
                        }

                        if (monster.getBukkitEntity().getLocation().getBlock().getType() == Material.WATER ||
                                monster.getBukkitEntity().getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
                            monster.setPosition(loc.getX() + 2, loc.getY(), loc.getZ() + 2);
                        }

                    } else {
                        SPAWNED_MONSTERS.remove(monster);
                    }
                }
            }
            if (spawnType.contains("*")) {
                spawnType = spawnType.replace("*", "");
            }

            if (isElite) {
                if (SPAWNED_MONSTERS.size() == 0) {
                    Location location = loc;
                    if (location.getBlock().getType() != Material.AIR
                            || location.add(0, 1, 0).getBlock().getType() != Material.AIR)
                        return;
                    Material type = location.getBlock().getType();
                    if (type == Material.ACACIA_STAIRS || type == Material.BIRCH_WOOD_STAIRS
                            || type == Material.COBBLESTONE_STAIRS || type == Material.DARK_OAK_STAIRS
                            || type == Material.JUNGLE_WOOD_STAIRS || type == Material.WOOD_STAIRS
                            || type == Material.STONE_SLAB2 || type == Material.DOUBLE_STONE_SLAB2
                            || type == Material.DOUBLE_STEP || type == Material.WOOD_DOUBLE_STEP)
                        return;
                    String mob = spawnType;
                    World world = armorstand.getWorld();
                    EnumMonster monsEnum = EnumMonster.getMonsterByString(mob);
                    if (monsEnum == null)
                        return;
                    Entity entity = SpawningMechanics.getMob(world, tier, monsEnum);
                    String customName = eliteName;
                    entity.setCustomName(customName.trim());
                    int level = Utils.getRandomFromTier(tier, lvlRange);
                    MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, tier, level);
                    EntityStats.setMonsterElite(entity, level, tier);

                    if (entity == null)
                        return;
                    toSpawn = true;
                    if (!firstSpawn) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                            world.addEntity(entity, SpawnReason.CUSTOM);
                            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                            toSpawn = false;
                            SPAWNED_MONSTERS.add(entity);
                        }, 1200 * 2L);
                    } else {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                            world.addEntity(entity, SpawnReason.CUSTOM);
                            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                            toSpawn = false;
                            firstSpawn = false;
                            SPAWNED_MONSTERS.add(entity);
                        });
                    }
                }
            } else if (SPAWNED_MONSTERS.size() < spawnAmount) {
                Location location = new Location(Bukkit.getWorlds().get(0), loc.getBlockX() + new Random().nextInt(6),
                        loc.getBlockY(), loc.getBlockZ() + new Random().nextInt(6));
                if (location.getBlock().getType() != Material.AIR
                        || location.add(0, 1, 0).getBlock().getType() != Material.AIR)
                    return;

                Material mat = location.getBlock().getType();
                if (mat == Material.ACACIA_STAIRS || mat == Material.BIRCH_WOOD_STAIRS
                        || mat == Material.COBBLESTONE_STAIRS || mat == Material.DARK_OAK_STAIRS
                        || mat == Material.JUNGLE_WOOD_STAIRS || mat == Material.WOOD_STAIRS
                        || mat == Material.STONE_SLAB2 || mat == Material.DOUBLE_STONE_SLAB2
                        || mat == Material.DOUBLE_STEP || mat == Material.WOOD_DOUBLE_STEP)
                    return;
                String mob = spawnType;
                World world = armorstand.getWorld();
                EnumEntityType type = EnumEntityType.HOSTILE_MOB;
                EnumMonster monsEnum = EnumMonster.getMonsterByString(mob);
                if (monsEnum == null) {
                    return;
                }
                Entity entity = SpawningMechanics.getMob(world, tier, monsEnum);
                int level = Utils.getRandomFromTier(tier, lvlRange);
                MetadataUtils.registerEntityMetadata(entity, type, tier, level);
                EntityStats.setMonsterRandomStats(entity, level, tier);

                String lvlName = ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";

                String customName = "";
                try {
                    customName = entity.getBukkitEntity().getMetadata("customname").get(0).asString();
                } catch (Exception exc) {
                    Utils.log.info(entity.getCustomName() + " doesn't have metadata 'customname' ");
                    customName = monsEnum.name;
                }

                if (!entity.getBukkitEntity().hasMetadata("elite"))
                    entity.setCustomName(lvlName + API.getTierColor(tier) + customName);
                toSpawn = true;
                if (!firstSpawn) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        world.addEntity(entity, SpawnReason.CUSTOM);
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        entity.getBukkitEntity().setVelocity(new Vector(0.25, 0.5, 0.25));
                        SPAWNED_MONSTERS.add(entity);
                        toSpawn = false;
                    }, 400L);
                } else {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        for (int i = 0; i < spawnAmount; i++) {
                            Entity newentity = SpawningMechanics.getMob(world, tier, monsEnum);
                            MetadataUtils.registerEntityMetadata(newentity, type, tier, level);
                            EntityStats.setMonsterRandomStats(newentity, level, tier);

                            String newlvlName = ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";
                            String newcustomName = "";
                            try {
                                newcustomName = newentity.getBukkitEntity().getMetadata("customname").get(0).asString();
                            } catch (Exception exc) {
                                Utils.log.info(newentity.getCustomName() + " doesn't have metadata 'customname' ");
                                newcustomName = monsEnum.name;
                            }

                            if (!newentity.getBukkitEntity().hasMetadata("elite"))
                                newentity.setCustomName(newlvlName + API.getTierColor(tier) + newcustomName);
                            newentity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                            world.addEntity(newentity, SpawnReason.CUSTOM);
                            newentity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                            newentity.getBukkitEntity().setVelocity(new Vector(0.1, 0, 0.1));
                            SPAWNED_MONSTERS.add(newentity);

                        }
                        firstSpawn = false;
                        toSpawn = false;
                    });
                }
            }
        } else {
            if (!SPAWNED_MONSTERS.isEmpty()) {
                if (!isElite)
                    kill();
            }
        }
    }

    /**
     * Kill all spawnedMonsters for this Mob Spawner
     */
    public void kill() {
        if (SPAWNED_MONSTERS.size() > 0)
            for (Entity spawnedMonster : SPAWNED_MONSTERS) {
                spawnedMonster.getBukkitEntity().remove();
                spawnedMonster.dead = true;
                armorstand.getWorld().kill(spawnedMonster);
            }
        firstSpawn = true;
        SPAWNED_MONSTERS.clear();
    }

    public void remove() {
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
     * Initialize spawner
     */
    public void init() {
    	Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), ()->{
        boolean notEmpty = API.getNearbyPlayers(loc, 35).isEmpty();
        if (!notEmpty) {
            timerID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                if (isRemoved) {
                    Bukkit.getScheduler().cancelTask(timerID);
                } else
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> spawnIn(notEmpty));
            }, 0, 100L);
        } else {
            if (timerID != -1) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    Bukkit.getScheduler().cancelTask(timerID);
                    timerID = -1;
                }, 20);
            }
        }
        }, 0, 80);
    }
}
