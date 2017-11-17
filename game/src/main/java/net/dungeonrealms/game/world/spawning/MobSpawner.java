package net.dungeonrealms.game.world.spawning;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.command.moderation.CommandOreEdit;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.CC;
import net.dungeonrealms.game.world.item.Item.ElementalAttribute;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;

/**
 * Base for a DR Mob Spawner.
 *
 * @author Unknown
 */
public abstract class MobSpawner implements Cloneable {

    @Getter //Where do we spawn them?
    private Location location;

    @Getter
    @Setter //What tier monster are we spawning?
    private int tier;

    @Getter //List of monsters we have spawned.
    private Set<Entity> spawnedMonsters = new ConcurrentSet<>();

    @Getter
    @Setter //How many do we spawn
    private int spawnAmount;

    @Setter
    @Getter //The timer that attempts to spawn in entities.
    private int timerID = -1;
    @Getter
    protected BukkitTask spawnerTask;

    @Getter
    @Setter
    private String world;

    @Getter //"high" or "low". How powerful this entity is.
    private String lvlRange;

    @Setter
    @Getter //Custom name
    protected String customName;

    @Setter
    @Getter //Monster type
    private EnumMonster monsterType;

    @Setter
    @Getter // Should we ignore spawn conditions?
    private boolean firstSpawn = true;

    @Getter
    private int initialRespawnDelay;

    @Setter //Delay between spawns.
    private int respawnDelay;

    @Setter
    @Getter
    private int counter = -1;

    @Setter
    @Getter
    private int minimumXZ;
    @Setter
    @Getter
    private int maximumXZ;

    @Getter
    @Setter
    private Hologram editHologram;

    @Getter
    @Setter
    private ItemType weaponType;

    @Getter
    @Setter
    private ElementalAttribute element;

    @Getter
    @Setter
    private double elementChance;

//    @Getter
//    private ArmorStand armorStand;

    @Getter
    @Setter
    private boolean dungeon;

    @Getter
    @Setter
    private int minMobScore = -1, maxMobScore = -1;

    @Getter
    @Setter
    private double minRarityScore = -1, maxRarityScore = -1;

    @Getter
    @Setter
    protected int toRespawn = 0;

    @Getter
    private UUID uuid = UUID.randomUUID();

    public MobSpawner(Location location, String world,EnumMonster type, String name, int tier, int spawnAmount, String lvlRange, int respawnDelay, int mininmumXZ, int maximumXZ, int minMobScore, int maxMobscore, double minRarityScore, double maxRarityScore) {
        setCustomName(name);
        setMonsterType(type);
        setSpawnAmount(Math.min(spawnAmount, 8));

        this.lvlRange = lvlRange;
        this.world = world;
        this.initialRespawnDelay = respawnDelay;
        setRespawnDelay(respawnDelay);

        this.spawnAmount = spawnAmount;
        this.location = location;
        this.tier = tier;
        this.respawnDelay = respawnDelay;
        this.counter = 0;
        this.minMobScore = minMobScore;
        this.maxMobScore = maxMobscore;
        this.minRarityScore = minRarityScore;
        this.maxRarityScore = maxRarityScore;
        setMinimumXZ(mininmumXZ);
        setMaximumXZ(maximumXZ);
//        spawnArmorStand();
    }
//
//    public void spawnArmorStand() {
//        ArmorStand as = getLocation().getWorld().spawn(getLocation(), ArmorStand.class);
//
//        // Remove old stands. (Shouldn't happen.)
//        List<org.bukkit.entity.Entity> list = as.getNearbyEntities(1, 1, 1);
//        list.stream().filter(entity -> entity instanceof ArmorStand).forEach(entity -> {
//            entity.remove();
//            if (as.getWorld().getBlockAt(getLocation()).getType() == Material.ARMOR_STAND)
//                as.getWorld().getBlockAt(getLocation()).setType(Material.AIR);
//        });
//
//        as.setVisible(false);
//        as.setGravity(false);
//        as.setInvulnerable(true);
//    }

    @Override
    @SneakyThrows
    public MobSpawner clone() {
        return (MobSpawner) super.clone();
    }

    public void createEditInformation() {
        if (this.editHologram != null && !this.editHologram.isDeleted())
            this.editHologram.delete();

        Hologram holo = HologramsAPI.createHologram(DungeonRealms.getInstance(), getLocation().clone().add(.5, 3.5, .5));

        if (this instanceof EliteMobSpawner) {
            EliteMobSpawner eliteSpawner = (EliteMobSpawner) this;

            EnumNamedElite elite = eliteSpawner.getEliteType();
            if (elite == null && hasCustomName())
                elite = EnumNamedElite.getFromName(getCustomName());

            holo.appendTextLine(ChatColor.GREEN + "Elite Type: " + (elite != null ? elite.toString() : "N/A"));
        }

        holo.appendTextLine(ChatColor.GREEN + "Mob Type: " + (getMonsterType() != null ? getMonsterType().name() : "N/A"));

        holo.appendTextLine(ChatColor.GREEN + "Tier: " + this.getTier());
        if (hasCustomName())
            holo.appendTextLine(ChatColor.GREEN + "Custom Name: " + ChatColor.RESET + getCustomName());

        holo.appendTextLine(ChatColor.GREEN + "Level Range: " + this.getLvlRange());
        holo.appendTextLine(ChatColor.GREEN + "Respawn Delay: " + this.getInitialRespawnDelay());
        holo.appendTextLine(ChatColor.GREEN + "Spawn Amount: " + this.getSpawnAmount());
        holo.appendTextLine(ChatColor.GREEN + "Spawn Range: " + this.getMinimumXZ() + " - " + this.getMaximumXZ());
        if(getMaxRarityScore() != -1)
            holo.appendTextLine(CC.Green + "Rarity Range: " + this.getMinRarityScore() + " - " + this.getMaxRarityScore());

        if (this.weaponType != null)
            holo.appendTextLine(ChatColor.GREEN + "Weapon Type: " + this.weaponType);

        if (getElement() != null)
            holo.appendTextLine(getElement().getColor() + "" + (getElementChance() != 0 ? getElementChance() : "100") + "% chance for " + getElement().getPrefix() + " damage");

        this.editHologram = holo;
    }

    public int getRespawnDelay() {
        int delay = respawnDelay;
        if (delay < 25)
            delay = getDelays()[getTier() - 1];

        //Dont really need to apply this?
//        if (this instanceof EliteMobSpawner)
        delay += delay / 12;

        return delay;
    }

    public int[] getDelays() {
        return new int[]{40, 80, 105, 145, 200};
    }

    public boolean hasCustomName() {
        return this.getCustomName() != null && !getCustomName().trim().isEmpty();
    }

    public String getSerializedString() {
        StringBuilder builder = new StringBuilder();
        Location loc = getLocation();

        builder.append("@#@");
        builder.append(minMobScore);
        builder.append("!");
        builder.append(maxMobScore);
        builder.append("!");
        builder.append(minRarityScore);
        builder.append("!");
        builder.append(maxRarityScore);
        builder.append("!");
        builder.append(getWorld());
        builder.append("@#@");

        builder.append(loc.getX()).append(",")
                .append(loc.getY()).append(",")
                .append(loc.getZ()).append("=");

        builder.append(getMonsterType().getIdName());

        if (this instanceof EliteMobSpawner) {
            builder.append("*");
        }
        if (hasCustomName())
            builder.append("(").append(getCustomName()).append(")");

        builder.append(":");

        // Tier
        builder.append(getTier()).append(";")
                .append(getSpawnAmount()).append(getLvlRange().equals("high") ? "+" : "-")
                .append("@");

        builder.append(this.initialRespawnDelay).append("#")
                .append(getMinimumXZ()).append("-").append(getMaximumXZ()).append("$");

        if (this.weaponType != null)
            builder.append("@WEP@").append(weaponType.name()).append("@WEP@");

        // Set element.
        if (getElement() != null) {
            builder.append("@ELEM@").append(getElement().name());

            if (getElementChance() > 0 && getElementChance() <= 100)
                builder.append("%").append(getElementChance());

            builder.append("@ELEM@");
        }

        return builder.toString();
    }

    public boolean checkSpawnTimer() {
        int spawned = getSpawnedMonsters().size();

        getSpawnedMonsters().forEach(ent -> {
            if (ent != null && (ent.isDead() || !ent.isValid())) {
                getSpawnedMonsters().remove(ent);
                SpawningMechanics.getMobSpawners().remove(ent.getUniqueId());
            }
        });

        if (getSpawnedMonsters().size() != spawned && getSpawnedMonsters().size() <= 0 && getCounter() == -1) {
            //All dead?
            setCounter(0);
            return true;
        }

        if (getCounter() >= 0) {
            setCounter(getCounter() + 1);
            //Its time to spawn them now?
            if (getCounter() >= getRespawnDelay()) {
                setCounter(-1);
                setFirstSpawn(true);
            }
        }
        return false;
    }

    public void spawnIn() {

        if (!isDungeon() && checkSpawnTimer()) return;

        if (getSpawnedMonsters().size() >= getSpawnAmount()) {
            return;
        }

        if (!canSpawnMobs()) {
//            System.out.println("Cannot spawn mobs!");
//            if (nowReady)
//                setCounter(-1);
            return;
        }

        createMobs();
        setFirstSpawn(false);
        setCounter(-1);
    }

    protected void createMobs() {
        spawn();
    }

    protected boolean canSpawnMobs() {
//        setCounter(getCounter() + 1);
        return isFirstSpawn() || getCounter() >= getRespawnDelay() || isDungeon() || toRespawn > 0;
    }

    public abstract void init();

    public void kill() {
        getSpawnedMonsters().stream().filter(e -> e != null).forEach(e -> {
            e.remove();
            SpawningMechanics.getMobSpawners().remove(e.getUniqueId());
        });
        getSpawnedMonsters().clear();

//        if (getArmorStand() != null)
//            getArmorStand().remove();
    }

    public void remove() {
        kill();
        Bukkit.getScheduler().cancelTask(getTimerID());

        SpawningMechanics.getSpawners().remove(this);
        SpawningMechanics.saveConfig();
    }

    protected Entity spawn() {
        if (toRespawn > 0) {
            toRespawn--;
        }

        Location spawn = spray();

        if (isDungeon() && this instanceof EliteMobSpawner) {
            Bukkit.getLogger().info("Creating elite at " + spawn.getBlockX() + "x " + spawn.getBlockY() + "y " + spawn.getBlockZ() + "z: " + getMonsterType());
        }

        if (GameAPI.isInSafeRegion(spawn))
            return null;

        if (getMonsterType() == null) {
            Utils.log.info("Could not spawn non-existant monster-type.");
            return null;
        }

        Entity entity;
        int level = Utils.getRandomFromTier(getTier(), getLvlRange());
        if (this instanceof EliteMobSpawner) {
            EliteMobSpawner ms = (EliteMobSpawner) this;
            entity = EntityAPI.spawnElite(spawn, getLocation().clone(), ms.getEliteType(), getMonsterType(), getTier(), level, getCustomName());
        } else {
            entity = EntityAPI.spawnCustomMonster(spawn, getLocation().clone(), getMonsterType(), level, getTier(), getWeaponType(), getCustomName(), getMinMobScore(), getMaxMobScore(), getMinRarityScore(), getMaxRarityScore());
        }

        if (entity == null) {
            Bukkit.getLogger().info("Unable to create entity: " + level + " At: " + spawn);
            return null;
        }

        getSpawnedMonsters().add(entity);
        SpawningMechanics.getMobSpawners().put(entity.getUniqueId(), this);

        return entity;
    }

    protected void setTimer(Runnable r, int delay) {
        if (getSpawnerTask() != null && Bukkit.getScheduler().isCurrentlyRunning(getSpawnerTask().getTaskId()))
            getSpawnerTask().cancel();

        spawnerTask = Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), () -> {
            r.run();
//            if (getTimerID() == -1)
//                setTimerID(Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), r, 0L, (long) delay).getTaskId());
        }, 0L, delay);
    }

    public boolean doesLineMatchLocation(String line) {
        if (!line.contains("=")) return false;
        String[] coords = line.split("=")[0].split(",");

        double x = Double.parseDouble(coords[0]);
        double y = Double.parseDouble(coords[1]);
        double z = Double.parseDouble(coords[2]);

        return getLocation().getX() == x && getLocation().getY() == y && getLocation().getZ() == z;
    }

    public Location spray() {
        Location loc = getLocation();
        double bound = getMinimumXZ() + getMaximumXZ();
        int xMin = (int) (loc.getX() - bound);
        int xMax = (int) (loc.getX() + bound);
        int zMin = (int) (loc.getZ() - bound);
        int zMax = (int) (loc.getZ() + bound);

        // Add 0.5 so they spawn in the middle of the block.
        double x = Utils.randInt(xMin, xMax) + 0.5D;
        double y = loc.getY() + 3D;
        double z = Utils.randInt(zMin, zMax) + 0.5D;

        // Raise mobs out of ground if they spawn in it.
        Location ret = new Location(loc.getWorld(), x, y, z);
        while (ret.getBlock().getType() != Material.AIR && ret.getY() < 250)
            ret.add(0, 1, 0);

        return ret;
    }
}
