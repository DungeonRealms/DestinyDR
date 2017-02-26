package net.dungeonrealms.game.world.spawning;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.world.entity.ElementalDamage;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityArmorStand;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class MobSpawner {

    @Getter
    protected Location loc;
    @Getter
    @Setter
    protected String spawnType;
    @Getter
    protected EntityArmorStand armorstand;
    @Getter
    protected int tier;
    @Getter
    protected List<Entity> SPAWNED_MONSTERS = new CopyOnWriteArrayList<>();
    @Getter
    protected int spawnAmount;
    @Getter
    protected int id;
    @Getter
    protected int timerID = -1;
    @Getter
    protected String lvlRange;
    @Getter
    protected String monsterCustomName;
    @Getter
    protected EnumMonster monsterType;
    @Getter
    protected boolean firstSpawn = true;
    @Getter
    @Setter
    protected boolean hasCustomName = false;

    @Setter
    @Getter
    protected int initialRespawnDelay;
    @Getter
    @Setter
    protected int respawnDelay;

    @Getter
    protected int counter;

    @Getter
    protected int mininmumXZ;
    @Getter
    protected int maximumXZ;

    @Getter
    @Setter
    private Hologram editHologram;

    @Getter
    @Setter
    private String weaponType;

    @Getter
    @Setter
    private String elementalDamage;
    @Getter
    @Setter
    private double elementChance;

    public MobSpawner(Location location, String type, int tier, int spawnAmount, int configid, String lvlRange, int respawnDelay, int mininmumXZ, int maximumXZ) {
        if (type.contains("(")) {
            hasCustomName = true;
            monsterCustomName = type.substring(type.indexOf("(") + 1, type.indexOf(")"));
            monsterCustomName = monsterCustomName.replaceAll("_", " ");
            type = type.substring(0, type.indexOf("("));
        }

        if (this instanceof EliteMobSpawner)
            type = type.replace("*", "");

        if (spawnAmount > 8)
            spawnAmount = 8;

        this.lvlRange = lvlRange;
        this.initialRespawnDelay = respawnDelay;

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
        List<org.bukkit.entity.Entity> list = armorstand.getBukkitEntity().getNearbyEntities(1, 1, 1);
        if (list.size() > 0) {
            list.stream().filter(entity -> entity instanceof ArmorStand).forEach(entity -> {
                entity.remove();
                ((ArmorStand) entity).setHealth(0);
                if (armorstand.getBukkitEntity().getWorld().getBlockAt(loc).getType() == Material.ARMOR_STAND)
                    armorstand.getBukkitEntity().getWorld().getBlockAt(loc).setType(Material.AIR);
            });
        }
        armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
        world.addEntity(armorstand, CreatureSpawnEvent.SpawnReason.CUSTOM);
        armorstand.setPosition(loc.getX(), loc.getY(), loc.getZ());
        armorstand.setInvisible(true);
    }

    public void createEditInformation() {
        if(this.editHologram != null && !this.editHologram.isDeleted()){
            this.editHologram.delete();
        }
        Hologram holo = HologramsAPI.createHologram(DungeonRealms.getInstance(), this.getLoc().clone().add(.5, 3.5, .5));

        if (this instanceof EliteMobSpawner) {
            EliteMobSpawner eliteSpawner = (EliteMobSpawner) this;

            EnumNamedElite elite = eliteSpawner.getEliteType();
            if ((elite == null || elite == EnumNamedElite.NONE) && this.monsterCustomName != null) {
                elite = EnumNamedElite.getFromName(this.monsterCustomName);
            }

            holo.appendTextLine(ChatColor.GREEN + "Elite Type: " + (elite != null ? elite.toString() : "N/A"));
        }

        holo.appendTextLine(ChatColor.GREEN + "Mob Type: " + (this.getSpawnType() != null ? this.getSpawnType() : "N/A"));

        holo.appendTextLine(ChatColor.GREEN + "Tier: " + this.getTier());
        if (this.getMonsterCustomName() != null)
            holo.appendTextLine(ChatColor.GREEN + "Custom Name: " + ChatColor.RESET + this.getMonsterCustomName());

        holo.appendTextLine(ChatColor.GREEN + "Level Range: " + this.getLvlRange());
        holo.appendTextLine(ChatColor.GREEN + "Respawn Delay: " + this.getRespawnDelay());
        holo.appendTextLine(ChatColor.GREEN + "Spawn Amount: " + this.getSpawnAmount());
        holo.appendTextLine(ChatColor.GREEN + "Spawn Range: " + this.getMininmumXZ() + " - " + this.getMaximumXZ());

        if (this.weaponType != null) {
            holo.appendTextLine(ChatColor.GREEN + "Weapon Type: " + this.weaponType);
        }
        if (this.elementalDamage != null) {
            ElementalDamage damage = ElementalDamage.getFromName(this.elementalDamage);
            if (damage != null)
                holo.appendTextLine(damage.getPrefixColor() + (this.elementChance != 0 ? this.elementChance + "% " : "100% ") + " chance for " + damage.getElementalDamagePrefix() + " damage");

        }
        this.editHologram = holo;
    }

    //coords=type*(name):tier;amount<high/low (lvl range)>@SpawnTime#rangeMin-rangMax$
    //x,y,z=type*(Name):4;1-@400#1-1$
    public String getSerializedString() {
        StringBuilder builder = new StringBuilder();

        builder.append(loc.getX()).append(",").append(loc.getY()).append(",").append(loc.getZ()).append("=");
        builder.append(spawnType);
        if (this instanceof EliteMobSpawner) {
            //Elite status.
            builder.append("*");
        }

//        builder.append(":");
        if (hasCustomName) {
            builder.append("(").append(monsterCustomName.replace(" ", "_")).append(")");
        }

        builder.append(":");
        //tier
        builder.append(tier).append(";").append(spawnAmount).append(lvlRange.equals("high") ? "+" : "-").append("@");
        builder.append(this.initialRespawnDelay).append("#").append(mininmumXZ).append("-").append(maximumXZ).append("$");

        if (this.weaponType != null) {
            builder.append("@WEP@").append(weaponType.toUpperCase()).append("@WEP@");
        }

        if (this.elementalDamage != null) {
            builder.append("@ELEM@").append(this.elementalDamage);

            if (this.elementChance != 100 && this.elementChance > 0) {
                builder.append("%").append(this.elementChance);
            }
            builder.append("@ELEM@");
        }
        return builder.toString();
    }

    public void setCustomName(String name) {
        this.hasCustomName = true;
        this.monsterCustomName = name;
    }

    public abstract void spawnIn();

    public abstract void init();

    public void kill() {
        if (SPAWNED_MONSTERS.size() > 0) {
            for (Entity spawnedMonster : SPAWNED_MONSTERS) {
                spawnedMonster.getBukkitEntity().remove();
                spawnedMonster.dead = true;
                armorstand.getWorld().kill(spawnedMonster);
            }
            SPAWNED_MONSTERS.clear();
        }
    }

    public void remove() {
        kill();
        armorstand.getWorld().removeEntity(armorstand);
        armorstand.getBukkitEntity().remove();

        for (String spawner : Lists.newArrayList(SpawningMechanics.SPAWNER_CONFIG)) {
            if(doesLineMatchLocation(getLoc(), spawner)){
                //Remove that whole line..
                SpawningMechanics.SPAWNER_CONFIG.remove(spawner);
                DungeonRealms.getInstance().getConfig().set("spawners", SpawningMechanics.SPAWNER_CONFIG);
                DungeonRealms.getInstance().saveConfig();
                Bukkit.getLogger().info("Removing spawner line: " + spawner);
                break;
            }
        }
    }

    public static boolean doesLineMatchLocation(Location location, String line){
        if(!line.contains("="))return false;
        String[] coords = line.split("=")[0].split(",");

        double x = Double.parseDouble(coords[0]);
        double y = Double.parseDouble(coords[1]);
        double z = Double.parseDouble(coords[2]);
        if (location.getX() == x && location.getY() == y && location.getZ() == z) {
            return true;
        }

        return false;
    }
}
