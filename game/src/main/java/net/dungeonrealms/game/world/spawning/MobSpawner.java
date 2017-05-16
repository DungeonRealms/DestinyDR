package net.dungeonrealms.game.world.spawning;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.Item.ElementalAttribute;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base for a DR Mob Spawner.
 *
 * @author Unknown
 */
public abstract class MobSpawner {

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

    @Getter //"high" or "low". How powerful this entity is.
    private String lvlRange;

    @Setter
    @Getter //Custom name
    private String customName;

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
    private int counter;

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

    @Getter
    private ArmorStand armorStand;

    @Getter
    @Setter
    private boolean dungeon;

    public MobSpawner(Location location, EnumMonster type, String name, int tier, int spawnAmount, String lvlRange, int respawnDelay, int mininmumXZ, int maximumXZ) {
        setCustomName(name);
        setMonsterType(type);
        setSpawnAmount(Math.min(spawnAmount, 8));

        this.lvlRange = lvlRange;
        this.initialRespawnDelay = respawnDelay;
        setRespawnDelay(respawnDelay);

        this.spawnAmount = spawnAmount;
        this.location = location;
        this.tier = tier;
        this.respawnDelay = respawnDelay;
        this.counter = 0;
        setMinimumXZ(mininmumXZ);
        setMaximumXZ(maximumXZ);
        spawnArmorStand();
    }

    private void spawnArmorStand() {
        ArmorStand as = getLocation().getWorld().spawn(getLocation(), ArmorStand.class);

        // Remove old stands. (Shouldn't happen.)
        List<org.bukkit.entity.Entity> list = as.getNearbyEntities(1, 1, 1);
        list.stream().filter(entity -> entity instanceof ArmorStand).forEach(entity -> {
            entity.remove();
            if (as.getWorld().getBlockAt(getLocation()).getType() == Material.ARMOR_STAND)
                as.getWorld().getBlockAt(getLocation()).setType(Material.AIR);
        });

        as.setVisible(false);
        as.setGravity(false);
        as.setInvulnerable(true);
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
        delay += delay / 10;
        return delay;
    }

    public int[] getDelays() {
        return new int[]{40, 80, 105, 145, 200};
    }

    public boolean hasCustomName() {
        return this.getCustomName() != null;
    }

    public String getSerializedString() {
        StringBuilder builder = new StringBuilder();
        Location loc = getLocation();

        builder.append(loc.getX()).append(",")
                .append(loc.getY()).append(",")
                .append(loc.getZ()).append("=");

        builder.append(getMonsterType().getIdName());

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

    public void spawnIn() {
        getSpawnedMonsters().stream().filter(ent -> ent != null && ent.isDead()).forEach(getSpawnedMonsters()::remove);

        if (getSpawnedMonsters().size() >= getSpawnAmount() || !canSpawnMobs())
            return;

        createMobs();
        setFirstSpawn(false);
        setCounter(0);
    }

    protected void createMobs() {
        spawn();
    }

    protected boolean canSpawnMobs() {
        setCounter(getCounter() + 1);
        return isFirstSpawn() || getCounter() >= getRespawnDelay() || isDungeon();
    }

    public abstract void init();

    public void kill() {
        getSpawnedMonsters().stream().filter(e -> e != null).forEach(ent -> ent.remove());
        getSpawnedMonsters().clear();

        if (getArmorStand() != null)
            getArmorStand().remove();
    }

    public void remove() {
        kill();
        Bukkit.getScheduler().cancelTask(getTimerID());

        SpawningMechanics.getSpawners().remove(this);
        SpawningMechanics.saveConfig();
    }

    protected Entity spawn() {
        Location spawn = spray();

        if (GameAPI.isInSafeRegion(spawn))
            return null;

        if (getMonsterType() == null) {
            Utils.log.info("Could not spawn non-existant monster-type.");
            return null;
        }

        Entity entity;
        int level = Utils.getRandomFromTier(getTier(), getLvlRange());
        System.out.println("Spawning " + getMonsterType().getIdName() + ". Tier = " + getTier() + ", Level = " + level);
        if (this instanceof EliteMobSpawner) {
            EliteMobSpawner ms = (EliteMobSpawner) this;
            entity = EntityAPI.spawnElite(spawn, ms.getEliteType(), getMonsterType(), getTier(), level, getCustomName());
        } else {
            entity = EntityAPI.spawnCustomMonster(spawn, getMonsterType(), level, getTier(), getWeaponType(), getCustomName());
        }

        if(entity == null){
            Bukkit.getLogger().info("Unable to create entity: " + level + " At: " + spawn);
            return null;
        }
        getSpawnedMonsters().add(entity);
        return entity;
    }

    protected void setTimer(Runnable r, int delay) {
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), () -> {
            if (getTimerID() == -1)
                setTimerID(Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), r, 0L, (long) delay).getTaskId());
        }, 0L, 40L);
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
