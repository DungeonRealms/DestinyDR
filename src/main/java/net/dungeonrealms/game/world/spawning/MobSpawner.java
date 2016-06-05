package net.dungeonrealms.game.world.spawning;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.DungeonManager.DungeonObject;
import net.dungeonrealms.game.miscellaneous.LocationUtils;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.boss.subboss.Pyromancer;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
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

    private Location loc;
    private String spawnType;
    private EntityArmorStand armorstand;
    private int tier;
    private List<Entity> SPAWNED_MONSTERS = new CopyOnWriteArrayList<>();
    private boolean isElite = false;
    private int spawnAmount;
    private int id;
    private int timerID = -1;
    private String lvlRange;
    private String monsterCustomName;
    private boolean firstSpawn = true;
    private boolean isDungeonSpawner;
    private boolean hasCustomName = false;
    private int respawnDelay;
    private int counter;

    public MobSpawner(Location location, String type, int tier, int spawnAmount, int configid, String lvlRange, int respawnDelay) {
        if (type.contains("(") && type.contains("*")) {
            isElite = true;
        } else if (type.contains("(")) {
            hasCustomName = true;
        }
        if (isElite || hasCustomName) {
            monsterCustomName = type.substring(type.indexOf("(") + 1, type.indexOf(")"));
            monsterCustomName = monsterCustomName.replace("_", " ");
            type = type.substring(0, type.indexOf("("));
            if (type.contains("*"))
                type = type.replace("*", "");
            if (isElite)
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
        this.respawnDelay = respawnDelay;
        this.counter = 0;
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
    private void spawnIn() {
        if (!SPAWNED_MONSTERS.isEmpty()) {
            for (Entity monster : SPAWNED_MONSTERS) {
                if (monster.isAlive()) {
                    if (API.isInSafeRegion(monster.getBukkitEntity().getLocation())) {
                        monster.setPosition(loc.getX(), loc.getY(), loc.getZ());
                    }
                    double num = monster.getBukkitEntity().getLocation().distanceSquared(loc);
                    if (num > 1600) {
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
                if (entity == null)
                    return;
                String customName = monsterCustomName.trim();
                entity.setCustomName(ChatColor.BOLD.toString() + API.getTierColor(tier) + customName.trim());
                int level = Utils.getRandomFromTier(tier, lvlRange);
                MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, tier, level);
                EntityStats.setMonsterElite(entity, level, tier);
                entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), ChatColor.BOLD.toString() + API.getTierColor(tier) + monsterCustomName.trim()));
                giveCustomEquipment(entity);
                if (LocationUtils.getNearbyEntities(location, 15).size() > 7) {
                    return; // Lets not spawn more than 15 entities in a radius of 7
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    world.addEntity(entity, SpawnReason.CUSTOM);
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    SPAWNED_MONSTERS.add(entity);
                    if (firstSpawn)
                        firstSpawn = false;
                }, firstSpawn ? 0 : 1200 * 2L);
            }
        } else if ((SPAWNED_MONSTERS.size() - 1) < spawnAmount) {
            if (!firstSpawn) {
                if (!canMobsSpawn()) {
                    counter = counter + 5;
                    //Mobs haven't passed their respawn timer yet.
                    return;
                }
            }
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
            if (entity == null) {
                return;
            }
            int level = Utils.getRandomFromTier(tier, lvlRange);
            MetadataUtils.registerEntityMetadata(entity, type, tier, level);
            EntityStats.setMonsterRandomStats(entity, level, tier);

            String lvlName = ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] ";

            String mobName;
            try {
                mobName = entity.getBukkitEntity().getMetadata("customname").get(0).asString();
            } catch (Exception exc) {
                mobName = monsEnum.name;
            }
            if (this.hasCustomName) {
                entity.setCustomName(lvlName + API.getTierColor(tier) + monsterCustomName);
                entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), API.getTierColor(tier) + ChatColor.BOLD.toString() + monsterCustomName));

            } else {
                entity.setCustomName(lvlName + API.getTierColor(tier) + mobName);
                entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), API.getTierColor(tier) + mobName));
            }

            if (firstSpawn) {
                firstSpawn = false;
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    int amountToSpawn = spawnAmount;
                    if (amountToSpawn > 3) {
                        amountToSpawn = 3;
                    }
                    for (int i = 0; i < amountToSpawn; i++) {
                        Entity newEntity = SpawningMechanics.getMob(world, tier, monsEnum);
                        int newLevel = Utils.getRandomFromTier(tier, lvlRange);
                        MetadataUtils.registerEntityMetadata(newEntity, type, tier, newLevel);
                        EntityStats.setMonsterRandomStats(newEntity, newLevel, tier);

                        String newlvlName = ChatColor.LIGHT_PURPLE.toString() + "[" + newLevel + "] ";

                        String newmobName = "";
                        try {
                            newmobName = newEntity.getBukkitEntity().getMetadata("customname").get(0).asString();
                        } catch (Exception exc) {
                            newmobName = monsEnum.name;
                        }
                        if (this.hasCustomName) {
                            newEntity.setCustomName(newlvlName + API.getTierColor(tier) + monsterCustomName);
                            newEntity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), API.getTierColor(tier) + ChatColor.BOLD.toString() + monsterCustomName));

                        } else {
                            newEntity.setCustomName(newlvlName + API.getTierColor(tier) + newmobName);
                            newEntity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), API.getTierColor(tier) + newmobName));
                        }
                        newEntity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        world.addEntity(newEntity, SpawnReason.CUSTOM);
                        newEntity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        newEntity.getBukkitEntity().setVelocity(new Vector(0.25, 0.5, 0.25));
                        SPAWNED_MONSTERS.add(newEntity);
                    }
                }, 40L);
            } else {
                entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                world.addEntity(entity, SpawnReason.CUSTOM);
                entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                entity.getBukkitEntity().setVelocity(new Vector(0.25, 0.5, 0.25));
                SPAWNED_MONSTERS.add(entity);
            }
        }
    }

    /**
     * @param entity
     */
    private void giveCustomEquipment(Entity entity) {
        if (monsterCustomName.equalsIgnoreCase("Blayshan The Naga")) {
            EntityInsentient ent = (EntityInsentient) entity;
            ent.setEquipment(0, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("blayshanaxe")));
            ent.setEquipment(1, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("blayshanboots")));
            ent.setEquipment(2, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("blayshanlegs")));
            ent.setEquipment(3, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("blayshanplate")));
            ent.setEquipment(4, CraftItemStack.asNMSCopy(ItemGenerator.getNamedItem("blayshanhelm")));
        }
    }

    /**
     * Custom Spawning for dungeons
     *
     * @param dungeon
     */
    public void dungeonSpawn(DungeonObject dungeon) {
        int i = 0;
        while (i < spawnAmount) {
            Location location = loc.add(new Random().nextInt(3), 1, new Random().nextInt(3));
            if (location.getBlock().getType() != Material.AIR
                    || location.add(0, 1, 0).getBlock().getType() != Material.AIR)
                return;
            String mob = spawnType;
            EnumMonster monsEnum = EnumMonster.getMonsterByString(mob);
            if (monsEnum == null)
                return;
            Entity entity = SpawningMechanics.getMob(((CraftWorld) loc.getWorld()).getHandle(), tier, monsEnum);
            if (hasCustomName)
                if (monsterCustomName.contains("Pyromancer")) {
                    entity = new Pyromancer(((CraftWorld) loc.getWorld()).getHandle(), location);
                    Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        for (Player p : API.getNearbyPlayers(loc, 400)) {
                            p.sendMessage(ChatColor.GREEN + EnumBoss.Pyromancer.name + ChatColor.RESET + ": " + EnumBoss.Pyromancer.greeting);
                        }
                    }, 20 * 60);
                }
            String customName = monsEnum.getPrefix() + " " + monsEnum.name + " " + monsEnum.getSuffix();
            int level = Utils.getRandomFromTier(tier, lvlRange);
            MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, tier, level);
            EntityStats.setMonsterRandomStats(entity, level, tier);
            String lvlName = ChatColor.LIGHT_PURPLE + "[" + level + "] ";
            if (entity == null) {
                return;
            }
            if (!hasCustomName) {
                entity.setCustomName(lvlName + API.getTierColor(tier) + customName);
                entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), API.getTierColor(tier) + customName));
            } else {
                entity.setCustomName(API.getTierColor(tier) + monsterCustomName);
                entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), API.getTierColor(tier) + monsterCustomName));
            }
            location.getWorld().loadChunk(location.getChunk());
            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
            ((CraftWorld) loc.getWorld()).getHandle().addEntity(entity, SpawnReason.CUSTOM);
            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
            dungeon.aliveMonsters.add(entity);
            i++;
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
                    }, 0, 100L);
                }
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

    /**
     * @param b
     */
    public void setDungeonSpawner(boolean b) {
        isDungeonSpawner = b;
    }

    //Checks whether mobs can spawn based on their delay set in config.
    private boolean canMobsSpawn() {
        if (counter < respawnDelay) {
            return false;
        }
        counter = 0;
        return true;
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

    public boolean isElite() {
        return isElite;
    }

    public void setElite(boolean elite) {
        isElite = elite;
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

    public boolean isDungeonSpawner() {
        return isDungeonSpawner;
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
}
