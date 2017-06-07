package net.dungeonrealms.game.mechanic.rifts;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.util.TimeUtil;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.skull.Skull;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class Rift {

    public static ItemStack skullItem = null;

    @Getter
    protected int x, y, z;

    @Getter
    protected int tier;

    protected transient Item.ElementalAttribute attribute;

    private String nearbyCity;

    private transient RiftState riftState;
    private transient Set<Entity> spawnedEntities = new ConcurrentSet<>();

    private transient Map<Location, MaterialData> changedBlocks = new ConcurrentHashMap<>();
    private transient int spawned = 0, aliveTime;
    private static final transient int MAX_ALIVE = 60 * 20;
    private transient long lastMobSpawn;
    private transient Hologram hologram;

    public Rift(Location spawn, int tier, Item.ElementalAttribute elementalType, String nearbyCity) {
        this.x = spawn.getBlockX();
        this.y = spawn.getBlockY();
        this.z = spawn.getBlockZ();
        this.tier = tier;
        this.attribute = elementalType;
        this.nearbyCity = nearbyCity.replace("_", " ");
        this.spawnedEntities = new ConcurrentSet<>();
    }

    //So will normally be 1 block above the ground.
    public Location getLocation() {
        return new Location(GameAPI.getMainWorld(), x, y, z);
    }

    public void onRiftEnd() {
        this.destroy();

        Bukkit.broadcastMessage(ChatColor.RED + "The Rift near " + getNearbyCity() + " has been sealed!");
    }

    public String getNearbyCity() {
        return nearbyCity.replace("_", " ");
    }

    public void onRiftStart() {
        spawned = aliveTime = 0;
        Bukkit.broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() + " *** " + ChatColor.RED + "A Rift is beginning to open near " + getNearbyCity() + "! " + ChatColor.BOLD + "***");
        Bukkit.broadcastMessage(ChatColor.GRAY + "Defeat the Rift Mobs to close it and receive a Rift Fragment!");
        this.riftState = RiftState.SPAWNING;
        Bukkit.getLogger().info("Creating rift at " + getLocation());
    }

    public void onRiftMinionDeath(Entity minion, EntityDeathEvent event) {
        this.spawnedEntities.remove(minion);
        if (this.getSpawnedEntities().size() == 0 && spawned >= getMaxMobLimit()) {
            //DONE?
            this.onRiftEnd();
        }

        if (EntityAPI.isElite(minion)) {
            //Drop the crystal??
            minion.getWorld().playSound(minion.getLocation(), Sound.ENTITY_ENDERMEN_DEATH, 3, .9F);

        }
    }

    public int getMaxMobLimit() {
        return tier * 10;
    }

    /**
     * Seconds in between each spawn?
     *
     * @return
     */
    public int getSpawnDelay() {
        return Math.max(1, tier / 2);
    }

    /**
     * Called around every 1 second? Seems suffice?
     */
    public void onRiftTick() {
        this.aliveTime++;
        Set<Entity> spawned = getSpawnedEntities();
        if (this.spawned >= getMaxMobLimit() && spawned.size() <= 0) {
            //Done?
            onRiftEnd();
            return;
        }

        if (this.spawned < getMaxMobLimit() && (System.currentTimeMillis() - lastMobSpawn) / 1000 >= getSpawnDelay()) {

            if (this.spawned == getMaxMobLimit() - 1) {
                //Its going to spawn the elite, make sure no mobs are left.
                Set<Entity> ent = getSpawnedEntities();
                if (ent.size() > 0) {
//                    Bukkit.getLogger().info("Not spawning Golem due to entities remaining!");
                    this.updateHologram();
                    return;
                }
            }
            spawnMob();
        }
        this.updateHologram();
    }

    public void createRift() {
        this.hologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), getLocation().add(.65, 3, .65));
        this.updateHologram();
        onRiftStart();
    }

    public void updateHologram() {
        if (this.hologram == null) return;
        String line1 = Item.ItemTier.getByTier(getTier()).getColor().toString() + ChatColor.BOLD + "Rift";
        String line2 = ChatColor.RED + "Closing in " + ChatColor.BOLD + TimeUtil.formatDifference(MAX_ALIVE - aliveTime);
        String line3 = ChatColor.RED.toString() + getSpawnedEntities().size() + " / " + getMaxMobLimit();
        if (this.hologram.size() < 3) {
            this.hologram.appendTextLine(line1);
            this.hologram.appendTextLine(line2);
            this.hologram.appendTextLine(line3);
        } else {
            ((TextLine) this.hologram.getLine(0)).setText(line1);
            ((TextLine) this.hologram.getLine(1)).setText(line2);
            ((TextLine) this.hologram.getLine(2)).setText(line3);
        }
    }

    public void destroy() {
        spawned = aliveTime = 0;
        riftState = RiftState.WAITING;
        if (!spawnedEntities.isEmpty()) {
            for (Entity ent : spawnedEntities) {
                ent.remove();
            }
            spawnedEntities.clear();
        }

        if (this.changedBlocks != null) {
            this.changedBlocks.forEach((loc, mat) -> loc.getBlock().setTypeIdAndData(mat.getItemTypeId(), mat.getData(), false));
            this.changedBlocks.clear();
        }

        this.hologram.delete();
        this.hologram = null;

//Not us anymore..
        RiftMechanics.getInstance().setActiveRift(null);
    }

    public Entity spawnMob() {
        this.spawned++;

        Location spawn = getLocation();
//        Location loc = Utils.getRandomLocationNearby(spawn, 3);
        Random r = ThreadLocalRandom.current();
        Location loc = r.nextBoolean() ? spawn.add(-3, 0, 0) : r.nextBoolean() ? spawn.add(0, 0, 4) : r.nextBoolean() ? spawn.add(4, 0, 0) : spawn.add(0, 0, -3);
        LivingEntity entity;
        if (this.spawned == getMaxMobLimit()) {
            entity = (LivingEntity) EntityAPI.spawnElite(loc.add(0, .35, 0), null, EnumMonster.Golem, tier, tier * 20, "Rift Walker");
            ParticleAPI.spawnParticle(Particle.PORTAL, entity.getLocation().clone().add(0, .5, 0), .5F, 50, .3F);
        } else {
            entity = (LivingEntity) EntityAPI.spawnCustomMonster(loc.add(0, .35, 0), EnumMonster.Acolyte, tier * 20, tier, null, "Rift Minion");
        }

        if (entity.getEquipment() != null && entity.getEquipment().getHelmet() != null && entity.getEquipment().getHelmet().getType().equals(Material.SKULL_ITEM)) {
            if (skullItem == null)
                skullItem = Skull.getCustomSkull("http://textures.minecraft.net/texture/f3f9bc52bed6e8dce5bd3b16457dee975241f898da3a29f857ef047b544a98");
            entity.getEquipment().setHelmet(skullItem);
        }

        entity.setRemoveWhenFarAway(false);
        MetadataUtils.Metadata.RIFT_MOB.set(entity, true);

        DRMonster monster = (DRMonster) ((CraftLivingEntity) entity).getHandle();
        if (monster.getAttributes() != null)
            monster.getAttributes().multiplyStat(Item.WeaponAttributeType.DAMAGE, 1.25);

        this.spawnedEntities.add(entity);
        return entity;
    }

    public Set<Entity> getSpawnedEntities() {
        if (this.spawnedEntities == null) this.spawnedEntities = new ConcurrentSet<>();

        for (Entity ent : this.spawnedEntities) {
            if (ent.isDead()) this.spawnedEntities.remove(ent);
        }

        return this.spawnedEntities;
    }

    public void changeBlock(Location location, Material toSet) {
        changeBlock(location.getBlock(), new MaterialData(toSet));
    }

    public void changeBlock(Location location, MaterialData toSet) {
        changeBlock(location.getBlock(), toSet);
    }

    public void changeBlock(Block blo, MaterialData toSet) {
        if (this.changedBlocks == null) this.changedBlocks = new ConcurrentHashMap<>();

        MaterialData current = new MaterialData(blo.getType(), blo.getData());

        MaterialData stored = this.changedBlocks.get(blo.getLocation());
        if (stored != null) {
            current = stored;
        }
        this.changedBlocks.put(blo.getLocation(), current);
        blo.setTypeIdAndData(toSet.getItemType().getId(), toSet.getData(), false);
    }
}
