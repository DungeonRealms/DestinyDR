package net.dungeonrealms.game.mechanic.rifts;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.google.common.collect.Lists;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.util.TimeUtil;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemArmorShield;
import net.dungeonrealms.game.item.items.functional.ItemRiftFragment;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.skull.Skull;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.List;
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

    public transient RiftState riftState;
    private transient Set<Entity> spawnedEntities = new ConcurrentSet<>();

    protected transient Map<Location, MaterialData> changedBlocks = new ConcurrentHashMap<>();
    private transient int spawned = 0, aliveTime;
    private static final transient int MAX_ALIVE = 60 * 20;
    private transient long lastMobSpawn;
    private transient List<Hologram> hologram;

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

        sendSealedMessage();
    }

    public void sendSealedMessage() {
        Bukkit.broadcastMessage(ChatColor.RED + "The Rift near " + getNearbyCity() + " has been sealed!");
    }

    public String getNearbyCity() {
        return nearbyCity.replace("_", " ");
    }

    public void onRiftStart() {
        spawned = aliveTime = 0;

        for (Player pl : Bukkit.getOnlinePlayers())
            Utils.sendCenteredMessage(pl, ChatColor.RED + ChatColor.BOLD.toString() + " *** " + ChatColor.RED + "A Rift is beginning to open near " + getNearbyCity() + "! " + ChatColor.BOLD + "***");

        Bukkit.broadcastMessage(ChatColor.GRAY + "Defeat the Rift Mobs to close it and receive a Rift Fragment!");
        this.riftState = RiftState.SPAWNING;
        Bukkit.getLogger().info("Creating rift at " + getLocation());
    }

    public boolean isRiftMinion(Entity entity) {
        if (this.spawnedEntities == null) return false;
        return this.spawnedEntities.contains(entity);
    }

    public void onRiftMinionDeath(Entity minion, EntityDeathEvent event) {

        Bukkit.getLogger().info("On Rift Minion Death!");
        this.spawnedEntities.remove(minion);
        if (this.getSpawnedEntities().size() == 0 && spawned >= getMaxMobLimit()) {
            //DONE?
            this.onRiftEnd();
        }

        if (EntityAPI.isElite(minion)) {
            //Drop the crystal??
            minion.getWorld().playSound(minion.getLocation(), Sound.ENTITY_ENDERMEN_DEATH, (float) (2 + Math.random()), (float) (Math.random() / 2 + .7F));

            if(ThreadLocalRandom.current().nextInt(10) == 2) {
                ItemStack shield = new ItemArmorShield().setTier(Item.ItemTier.getByTier(getTier())).setRarity(ThreadLocalRandom.current().nextInt(100) > 90 ? Item.ItemRarity.UNCOMMON : Item.ItemRarity.COMMON).setGlowing(true).generateItem();
                minion.getWorld().dropItem(minion.getLocation().add(0, 1, 0), shield);
            }

          //Drop shit?
            ItemStack item = new ItemRiftFragment(Item.ItemTier.getByTier(getTier())).generateItem();
            item.setAmount(ThreadLocalRandom.current().nextInt(3) + 1);
            minion.getWorld().dropItem(minion.getLocation().add(0, 1, 0), item);

            if (ThreadLocalRandom.current().nextInt(100_000) == 5) {
                minion.getWorld().playSound(minion.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, (float) (5 + Math.random()), .7F);
                minion.getWorld().dropItem(minion.getLocation().add(0, 1, 0), new ItemArmor().setType(ItemType.SHIELD).setRarity(Item.ItemRarity.COMMON).setTier(Item.ItemTier.getByTier(getTier())).generateItem());
                ParticleAPI.spawnParticle(Particle.CRIT, minion.getLocation().add(0, 1, 0), 30, .3F, .4F);
            }
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
                    this.updateHologram();
                    return;
                }
            }
            spawnMob();
        }
        this.updateHologram();
    }

    public void createRift() {
        if (this.hologram == null) this.hologram = Lists.newArrayList();
        this.hologram.add(HologramsAPI.createHologram(DungeonRealms.getInstance(), getLocation().add(3, 5, 1.5)));
        this.hologram.add(HologramsAPI.createHologram(DungeonRealms.getInstance(), getLocation().add(3, 5, -1.5)));
        this.updateHologram();
        onRiftStart();
    }

    public void updateHologram() {
        if (this.hologram == null) return;
        String line1 = Item.ItemTier.getByTier(getTier()).getColor().toString() + ChatColor.BOLD + "Rift";
        String line2 = ChatColor.RED + "Closing in " + ChatColor.BOLD + TimeUtil.formatDifference(MAX_ALIVE - aliveTime);
        String line3 = ChatColor.RED.toString() + ChatColor.BOLD + getSpawnedEntities().size() + " Alive";
        for (Hologram holo : hologram) {
            if (holo.size() < 3) {
                holo.appendTextLine(line1);
                holo.appendTextLine(line2);
                holo.appendTextLine(line3);
            } else {
                ((TextLine) holo.getLine(0)).setText(line1);
                ((TextLine) holo.getLine(1)).setText(line2);
                ((TextLine) holo.getLine(2)).setText(line3);
            }
        }
    }

    public void destroy() {
        this.spawned = aliveTime = 0;
        this.riftState = RiftState.WAITING;
        if (!this.spawnedEntities.isEmpty()) {
            for (Entity ent : this.spawnedEntities) {
                ent.remove();
            }
            this.spawnedEntities.clear();
        }

        this.returnBlocks();

        if (this.hologram != null && this.hologram.size() > 0) {
            this.hologram.forEach(h -> h.delete());
            this.hologram.clear();
        }

        RiftMechanics.getInstance().setActiveRift(null);
    }

    public void returnBlocks() {
        if (this.changedBlocks != null) {
            this.changedBlocks.forEach((loc, mat) -> loc.getBlock().setTypeIdAndData(mat.getItemTypeId(), mat.getData(), false));
            this.changedBlocks.clear();
        }
    }

    public Entity spawnMob() {
        this.spawned++;

        Item.ItemTier t = Item.ItemTier.getByTier(tier);
        Location spawn = getLocation();
//        Location loc = Utils.getRandomLocationNearby(spawn, 3);
        Random r = ThreadLocalRandom.current();

        boolean leftSide = r.nextBoolean();
        Location loc = leftSide ? spawn.add(1.5, 10 + r.nextInt(2), -1) : spawn.add(1.5, 10 + r.nextInt(2), 1);
        LivingEntity entity;
        if (this.spawned == getMaxMobLimit()) {
            entity = (LivingEntity) EntityAPI.spawnElite(loc.add(0, .35, 0), null, EnumMonster.WitherSkeleton, tier, tier * 20, t.getColor() + ChatColor.BOLD.toString() + "Rift Walker");
            ParticleAPI.spawnParticle(Particle.PORTAL, entity.getLocation().clone().add(0, .5, 0), .5F, 50, .3F);
            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_WITHER_SPAWN, 3, 1.4F);
            entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(.4F);
            entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(35);
        } else {
            entity = (LivingEntity) EntityAPI.spawnCustomMonster(loc.add(0, .35, 0), r.nextInt(10) == 5 ? EnumMonster.StaffZombie : EnumMonster.Skeleton, tier * 20, tier, null, "Rift Minion");
            entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(20);
        }

        if (leftSide) {
            entity.setVelocity(new Vector(Math.random() / 2, 0, Math.max(-.45F, -Math.random() * .75)));
        } else {
            entity.setVelocity(new Vector(Math.random() / 2, 0, Math.max(.45F, Math.random() * .75)));
        }

        boolean elite = EntityAPI.isElite(entity);
        if (entity.getEquipment() != null && entity.getEquipment().getHelmet() != null && entity.getEquipment().getHelmet().getType().equals(Material.SKULL_ITEM)) {
            if (skullItem == null)
                skullItem = Skull.getCustomSkull("http://textures.minecraft.net/texture/f3f9bc52bed6e8dce5bd3b16457dee975241f898da3a29f857ef047b544a98");
            entity.getEquipment().setHelmet(skullItem);
        }

        entity.setRemoveWhenFarAway(false);
        MetadataUtils.Metadata.RIFT_MOB.set(entity, true);

        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1.1F, 1.1F);
        DRMonster monster = (DRMonster) ((CraftLivingEntity) entity).getHandle();

        if (r.nextInt(4) == 0 || elite) {
            ItemStack shield = new ItemArmor().setType(ItemType.SHIELD).setRarity(Item.ItemRarity.RARE).setTier(getTier()).generateItem();
            entity.getEquipment().setItemInOffHand(shield);
            monster.calculateAttributes();
        }

        if (monster.getAttributes() != null) {
            if (elite)
                monster.getAttributes().multiplyStat(Item.ArmorAttributeType.HEALTH_POINTS, 1.5);
            monster.getAttributes().multiplyStat(Item.WeaponAttributeType.DAMAGE, 1.25);
        }
        this.spawnedEntities.add(entity);
        return entity;
    }

    public Set<Entity> getSpawnedEntities() {
        if (this.spawnedEntities == null) this.spawnedEntities = new ConcurrentSet<>();

        for (Entity ent : this.spawnedEntities) {
            if (ent.isDead() || !ent.isValid()) {
                Bukkit.getLogger().info("Removing dead entity: " + ent);
                spawned--;
                this.spawnedEntities.remove(ent);
            }
        }

        return this.spawnedEntities;
    }

    public Block changeBlock(Block block, Material toSet) {
        return changeBlock(block, new MaterialData(toSet));
    }

    public Block changeBlock(Location location, Material toSet) {
        return changeBlock(location.getBlock(), new MaterialData(toSet));
    }

    public Block changeBlock(Location location, MaterialData toSet) {
        return changeBlock(location.getBlock(), toSet);
    }

    public Block changeBlock(Block blo, MaterialData toSet) {
        if (this.changedBlocks == null) this.changedBlocks = new ConcurrentHashMap<>();

        MaterialData current = new MaterialData(blo.getType(), blo.getData());

        MaterialData stored = this.changedBlocks.get(blo.getLocation());
        if (stored != null) {
            current = stored;
        }
        this.changedBlocks.put(blo.getLocation(), current);
        blo.setTypeIdAndData(toSet.getItemType().getId(), toSet.getData(), false);
        return blo;
    }
}
