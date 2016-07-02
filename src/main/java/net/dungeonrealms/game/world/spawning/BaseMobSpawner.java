package net.dungeonrealms.game.world.spawning;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityArmorStand;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Chase on Sep 25, 2015
 */
public class BaseMobSpawner {

    private Location loc;
    private String spawnType;
    private EntityArmorStand armorstand;
    private int tier;
    private List<Entity> SPAWNED_MONSTERS = new CopyOnWriteArrayList<>();
    private Map<Entity, Integer> RESPAWN_TIMES = new ConcurrentHashMap<>();
    private int spawnAmount;
    private int id;
    private int timerID = -1;
    private String lvlRange;
    private String monsterCustomName;
    private EnumMonster monsterType;
    private boolean firstSpawn = true;
    private boolean hasCustomName = false;
    private int respawnDelay;
    private int counter;
    private int mininmumXZ;
    private int maximumXZ;

    public BaseMobSpawner(Location location, String type, int tier, int spawnAmount, int configid, String lvlRange, int respawnDelay, int mininmumXZ, int maximumXZ) {
        if (type.contains("(")) {
            hasCustomName = true;
        }
        if (hasCustomName) {
            monsterCustomName = type.substring(type.indexOf("(") + 1, type.indexOf(")"));
            monsterCustomName = monsterCustomName.replace("_", " ");
            type = type.substring(0, type.indexOf("("));
        }
        if (spawnAmount > 6) {
            spawnAmount = 6;
        }
        this.lvlRange = lvlRange;
        this.spawnAmount = spawnAmount;
        this.loc = location;
        this.id = configid;
        this.spawnType = type;
        this.tier = tier;
        this.respawnDelay = respawnDelay;
        this.counter = 0;
        this.mininmumXZ = mininmumXZ;
        this.maximumXZ = maximumXZ;
        World world = ((CraftWorld) location.getWorld()).getHandle();
        armorstand = new EntityArmorStand(world);
        armorstand.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "spawner"));
        armorstand.getBukkitEntity().setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), tier));
        armorstand.getBukkitEntity().setMetadata("monsters", new FixedMetadataValue(DungeonRealms.getInstance(), type));
        List<org.bukkit.entity.Entity> list = armorstand.getBukkitEntity().getNearbyEntities(loc.getX(), loc.getY(), loc.getZ());
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
    private void spawnIn() {
        if (!SPAWNED_MONSTERS.isEmpty()) {
            for (Entity monster : SPAWNED_MONSTERS) {
                if (monster.isAlive()) {
                    LivingEntity livingEntity = (LivingEntity) monster.getBukkitEntity();
                    if (API.isInSafeRegion(livingEntity.getLocation())) {
                        if (livingEntity instanceof Creature) {
                            ((Creature) livingEntity).setTarget(null);
                        }
                        monster.setPosition(loc.getX() + 2, loc.getY(), loc.getZ() + 2);
                        continue;
                    }
                    double num = livingEntity.getLocation().distanceSquared(loc);
                    if (num > 700) {
                        monster.setPosition(loc.getX() + 2, loc.getY(), loc.getZ() + 2);
                        if (livingEntity instanceof Creature) {
                            ((Creature) livingEntity).setTarget(null);
                        }
                    }
                } else {
                    RESPAWN_TIMES.put(monster, respawnDelay);
                    SPAWNED_MONSTERS.remove(monster);
                }
            }
        }
        if (SPAWNED_MONSTERS.size() < spawnAmount) {
            if (!firstSpawn) {
                if (!canMobSpawn()) {
                    //Mobs haven't passed their respawn timer yet.
                    return;
                }
            } else {
                RESPAWN_TIMES.clear();
            }
            Location location = getRandomLocation(loc, ((loc.getX() - mininmumXZ) - maximumXZ), ((loc.getX() + mininmumXZ) + maximumXZ),
                    ((loc.getZ() - mininmumXZ) - maximumXZ), ((loc.getZ() + mininmumXZ) + maximumXZ));
            if (location.getBlock().getType() != Material.AIR) {
                if (location.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                    location.add(0, 1, 0);
                } else if (location.clone().add(0, 2, 0).getBlock().getType() == Material.AIR) {
                    location.add(0, 2, 0);
                } else {
                    counter = respawnDelay;
                    return;
                }
            }
            if (API.isInSafeRegion(location)) {
                counter = respawnDelay;
                return;
            }
            World world = armorstand.getWorld();
            EnumEntityType type = EnumEntityType.HOSTILE_MOB;
            if (monsterType == null) {
                String mob = spawnType;
                if (hasCustomName) {
                    if (monsterCustomName.toLowerCase().contains("undead")) {
                        String spawnTypeLower = spawnType.toLowerCase();
                        if (!spawnTypeLower.equals("skeleton") && !spawnTypeLower.equals("skeleton1") && !spawnTypeLower.equals("skeleton2")) {
                            mob = "undead";
                        }
                    } else if (monsterCustomName.toLowerCase().contains("mountain")) {
                        mob = "frozenskeleton";
                    }
                }
                //TODO: Fix for Mooshroom
                if (mob.toLowerCase().contains("mooshroom")) {
                    mob = "cow";
                }
                monsterType = EnumMonster.getMonsterByString(mob);
                if (monsterType == null) {
                    DungeonRealms.getInstance().getLogger().warning(mob + " does not exist in EnumMonster. Please add it.");
                    return;
                }
            }
            Entity entity = SpawningMechanics.getMob(world, tier, monsterType);
            if (entity == null) {
                return;
            }
            if (!isFriendlyMob(monsterType)) {
                int level = Utils.getRandomFromTier(tier, lvlRange);
                EntityStats.setMonsterRandomStats(entity, level, tier);
                SpawningMechanics.rollElement(entity, monsterType);
                String lvlName = ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";
                String mobName;
                try {
                    mobName = entity.getBukkitEntity().getMetadata("customname").get(0).asString();
                } catch (Exception exc) {
                    mobName = monsterType.name.trim();
                }
                if (this.hasCustomName) {
                    entity.setCustomName(lvlName + API.getTierColor(tier) + monsterCustomName.trim());
                    entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), API.getTierColor(tier) + ChatColor.BOLD.toString() + monsterCustomName.trim()));

                } else {
                    entity.setCustomName(lvlName + API.getTierColor(tier) + mobName.trim());
                    entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), API.getTierColor(tier) + mobName.trim()));
                }
            }

            if (firstSpawn) {
                firstSpawn = false;
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    int amountToSpawn = spawnAmount;
                    if (amountToSpawn > 3) {
                        amountToSpawn = 3;
                    }
                    for (int i = 0; i < amountToSpawn; i++) {
                        Entity newEntity = SpawningMechanics.getMob(world, tier, monsterType);
                        Location firstSpawn = getRandomLocation(loc, ((loc.getX() - mininmumXZ) - maximumXZ), ((loc.getX() + mininmumXZ) + maximumXZ), ((loc.getZ() - mininmumXZ) - maximumXZ), ((loc.getZ() + mininmumXZ) + maximumXZ));
                        if (firstSpawn.getBlock().getType() != Material.AIR) {
                            if (firstSpawn.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                                firstSpawn.add(0, 1, 0);
                            } else if (firstSpawn.clone().add(0, 2, 0).getBlock().getType() == Material.AIR) {
                                firstSpawn.add(0, 2, 0);
                            } else {
                                return;
                            }
                        }
                        if (API.isInSafeRegion(firstSpawn)) {
                            return;
                        }
                        if (newEntity == null) {
                            return;
                        }
                        if (!isFriendlyMob(monsterType)) {
                            int newLevel = Utils.getRandomFromTier(tier, lvlRange);
                            EntityStats.setMonsterRandomStats(newEntity, newLevel, tier);
                            SpawningMechanics.rollElement(newEntity, monsterType);
                            String newLevelName = ChatColor.LIGHT_PURPLE.toString() + "[" + newLevel + "] ";
                            String newMobName = "";
                            try {
                                newMobName = newEntity.getBukkitEntity().getMetadata("customname").get(0).asString();
                            } catch (Exception exc) {
                                newMobName = monsterType.name.trim();
                            }
                            if (this.hasCustomName) {
                                newEntity.setCustomName(newLevelName + API.getTierColor(tier) + monsterCustomName.trim());
                                newEntity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), API.getTierColor(tier) + ChatColor.BOLD.toString() + monsterCustomName.trim()));
                            } else {
                                newEntity.setCustomName(newLevelName + API.getTierColor(tier) + newMobName.trim());
                                newEntity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), API.getTierColor(tier) + newMobName.trim()));
                            }
                        }
                        newEntity.setLocation(firstSpawn.getX(), firstSpawn.getY(), firstSpawn.getZ(), 1, 1);
                        world.addEntity(newEntity, SpawnReason.CUSTOM);
                        newEntity.setLocation(firstSpawn.getX(), firstSpawn.getY(), firstSpawn.getZ(), 1, 1);
                        SPAWNED_MONSTERS.add(newEntity);
                    }
                }, 5L);
            } else {
                entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                world.addEntity(entity, SpawnReason.CUSTOM);
                entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                SPAWNED_MONSTERS.add(entity);
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

    private boolean isRemoved = false;

    /**
     * @return
     */
    public List<Entity> getSpawnedMonsters() {
        return SPAWNED_MONSTERS;
    }

    /**
     * Initialize spawner
     */
    void init() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            boolean playersNearby = !API.getNearbyPlayers(loc, 32).isEmpty();
            if (playersNearby) {
                if (timerID == -1) {
                    timerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                        if (isRemoved) {
                            Bukkit.getScheduler().cancelTask(timerID);
                        } else
                            spawnIn();
                    }, 0L, 20L);
                }
            } else {
                if (timerID != -1) {
                    Bukkit.getScheduler().cancelTask(timerID);
                    firstSpawn = true;
                    timerID = -1;
                }
            }
        }, 0L, 40L);
    }

    //Checks whether mobs can spawn based on their delay set in config.
    private boolean canMobSpawn() {
        if (!RESPAWN_TIMES.isEmpty()) {
            for (Map.Entry<Entity, Integer> entry : RESPAWN_TIMES.entrySet()) {
                int respawnTime = entry.getValue();
                Entity entity = entry.getKey();
                if (respawnTime > 0) {
                    respawnTime--;
                    RESPAWN_TIMES.put(entity, respawnTime);
                } else {
                    RESPAWN_TIMES.remove(entity);
                    return true;
                }
            }
        } else {
            if (counter >= respawnDelay) {
                return true;
            } else {
                counter++;
                return false;
            }
        }
        return false;
    }

    private boolean isFriendlyMob(EnumMonster monsterType) {
        return monsterType == EnumMonster.Pig || monsterType == EnumMonster.Cow || monsterType == EnumMonster.Bat || monsterType == EnumMonster.Ocelot;
    }

    private Location getRandomLocation(Location location, double xMin, double xMax, double zMin, double zMax) {
        org.bukkit.World world = location.getWorld();

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

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public String getSpawnType() {
        return spawnType;
    }

    public void setSpawnType(String spawnType) {
        this.spawnType = spawnType;
    }

    public EntityArmorStand getArmorstand() {
        return armorstand;
    }

    public void setArmorstand(EntityArmorStand armorstand) {
        this.armorstand = armorstand;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public List<Entity> getSPAWNED_MONSTERS() {
        return SPAWNED_MONSTERS;
    }

    public void setSPAWNED_MONSTERS(List<Entity> SPAWNED_MONSTERS) {
        this.SPAWNED_MONSTERS = SPAWNED_MONSTERS;
    }

    public int getSpawnAmount() {
        return spawnAmount;
    }

    public void setSpawnAmount(int spawnAmount) {
        this.spawnAmount = spawnAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTimerID() {
        return timerID;
    }

    public void setTimerID(int timerID) {
        this.timerID = timerID;
    }

    public String getLvlRange() {
        return lvlRange;
    }

    public void setLvlRange(String lvlRange) {
        this.lvlRange = lvlRange;
    }

    public String getMonsterCustomName() {
        return monsterCustomName;
    }

    public void setMonsterCustomName(String monsterCustomName) {
        this.monsterCustomName = monsterCustomName;
    }

    public boolean isFirstSpawn() {
        return firstSpawn;
    }

    public void setFirstSpawn(boolean firstSpawn) {
        this.firstSpawn = firstSpawn;
    }

    public boolean isHasCustomName() {
        return hasCustomName;
    }

    public void setHasCustomName(boolean hasCustomName) {
        this.hasCustomName = hasCustomName;
    }

    public int getRespawnDelay() {
        return respawnDelay;
    }

    public void setRespawnDelay(int respawnDelay) {
        this.respawnDelay = respawnDelay;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setRemoved(boolean removed) {
        isRemoved = removed;
    }

    public EnumMonster getMonsterType() {
        return monsterType;
    }

    public void setMonsterType(EnumMonster monsterType) {
        this.monsterType = monsterType;
    }

    public int getMaximumXZ() {
        return maximumXZ;
    }

    public int getMininmumXZ() {
        return mininmumXZ;
    }
}
