package net.dungeonrealms.game.world.spawning;

import com.google.common.collect.Lists;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.entity.util.EntityStats;
import net.dungeonrealms.game.world.item.*;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.*;
import net.minecraft.server.v1_9_R2.Item;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Kieran Quigley (Proxying) on 10-Jun-16.
 */
public class EliteMobSpawner extends MobSpawner {

    private List<Entity> SPAWNED_MONSTERS = new CopyOnWriteArrayList<>();
    private EnumMonster monsterType;
    private EnumNamedElite eliteType;
    @Setter
    private boolean isRemoved = false;

    public EliteMobSpawner(Location loc, String type, int tier, int configid, String lvlRange, int respawnDelay, int mininmumXZ, int maximumXZ) {
//        if (type.contains("(")) {
//            hasCustomName = true;
//            customName = type.substring(type.indexOf("(") + 1, type.indexOf(")"));
//            customName = customName.replaceAll("_", " ");
//            type = type.substring(0, type.indexOf("("));
//        }
//        type = type.replace("*", "");
//        this.levelRange = lvlRange;
//        this.location = loc;
//        this.id = configid;
//        this.spawnType = type;
//        this.tier = tier;
//        this.respawnDelay = respawnDelay;
//        this.counter = 0;
//        this.mininmumXZ = mininmumXZ;
//        this.maximumXZ = maximumXZ;
//        World world = ((CraftWorld) location.getWorld()).getHandle();
//        armorstand = new EntityArmorStand(world);
//        armorstand.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "spawner"));
//        armorstand.getBukkitEntity().setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), tier));
//        armorstand.getBukkitEntity().setMetadata("monsters", new FixedMetadataValue(DungeonRealms.getInstance(), type));
//        List<org.bukkit.entity.Entity> list = armorstand.getBukkitEntity().getNearbyEntities(1, 1, 1);
//        if (list.size() > 0) {
//            list.stream().filter(entity -> entity instanceof ArmorStand).forEach(entity -> {
//                entity.remove();
//                ((ArmorStand) entity).setHealth(0);
//                if (armorstand.getBukkitEntity().getWorld().getBlockAt(location).getType() == Material.ARMOR_STAND)
//                    armorstand.getBukkitEntity().getWorld().getBlockAt(location).setType(Material.AIR);
//            });
//        }
//        armorstand.setPosition(location.getX(), location.getY(), location.getZ());
//        world.addEntity(armorstand, CreatureSpawnEvent.SpawnReason.CUSTOM);
//        armorstand.setPosition(location.getX(), location.getY(), location.getZ());
        super(loc, type, tier, 1, configid, lvlRange, respawnDelay, mininmumXZ, maximumXZ);
    }

    public void init() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            if (timerID == -1) {
                timerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                    if (isRemoved) {
                        Bukkit.getScheduler().cancelTask(timerID);
                    } else
                        spawnIn();
                }, 0L, 20L);
            }
        }, 0L, 40L);
    }

    public void spawnIn() {
        boolean playersNearby = GameAPI.arePlayersNearby(loc, 24);
        if (!SPAWNED_MONSTERS.isEmpty()) {
            for (Entity monster : SPAWNED_MONSTERS) {
                LivingEntity livingEntity = (LivingEntity) monster.getBukkitEntity();
                if (monster.isAlive()) {
                    if (GameAPI.isInSafeRegion(livingEntity.getLocation())) {
                        if (livingEntity instanceof Creature) {
                            ((Creature) livingEntity).setTarget(null);
                        }
                        monster.setPosition(loc.getX(), loc.getY(), loc.getZ());
                        return;
                    }
                    if (livingEntity.getLocation().distance(loc) >= 35) {
                        if (livingEntity instanceof Creature) {
                            ((Creature) livingEntity).setTarget(null);
                        }
                        monster.setPosition(loc.getX() + 2, loc.getY(), loc.getZ() + 2);
                    }
                } else {
                    SPAWNED_MONSTERS.remove(monster);
                }
            }
        }
        if (SPAWNED_MONSTERS.isEmpty()) {
            if (!canMobsSpawn(playersNearby)) {
                //Mobs haven't passed their respawn timer yet.
                return;
            }
            Location toSpawn = getRandomLocation(loc, ((loc.getX() - mininmumXZ) - maximumXZ), ((loc.getX() + mininmumXZ) + maximumXZ),
                    ((loc.getZ() - mininmumXZ) - maximumXZ), ((loc.getZ() + mininmumXZ) + maximumXZ));
            if (toSpawn.getBlock().getType() != Material.AIR) {
                if (toSpawn.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
                    toSpawn.add(0, 1, 0);
                } else if (toSpawn.clone().add(0, 2, 0).getBlock().getType() == Material.AIR) {
                    toSpawn.add(0, 2, 0);
                } else {
                    counter = respawnDelay;
                    return;
                }
            }
            if (GameAPI.isInSafeRegion(toSpawn)) {
                counter = respawnDelay;
                return;
            }
            World world = armorstand.getWorld();
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
                    } else if (monsterCustomName.toLowerCase().contains("daemon")) {
                        mob = "daemon2";
                    }
                }
                monsterType = EnumMonster.getMonsterByString(mob);
                if (monsterType == null) {
                    DungeonRealms.getInstance().getLogger().warning(mob + " does not exist in EnumMonster. Please add it.");
                    return;
                }
            }
            if (eliteType == null) {
                if (hasCustomName) {
                    for (EnumNamedElite namedElite : EnumNamedElite.values()) {
                        if (namedElite.getConfigName().equalsIgnoreCase(monsterCustomName)) {
                            eliteType = namedElite;
                        }
                    }
                    if (eliteType == null) {
                        eliteType = EnumNamedElite.NONE;
                    }
                } else {
                    eliteType = EnumNamedElite.NONE;
                }
            }
            Entity entity;
            if (eliteType == EnumNamedElite.NONE) {
                entity = SpawningMechanics.getMob(world, tier, monsterType);
            } else {
                entity = SpawningMechanics.getEliteMob(world, tier, eliteType);
            }
            if (entity == null) {
                Bukkit.getLogger().info("Unable to create elite mob " + eliteType + " at " + toSpawn.toString());
                return;
            }
            int level = Utils.getRandomFromTier(tier, lvlRange);
            EntityStats.setMonsterElite(entity, eliteType, tier, monsterType, level, false);
            giveCustomEquipment(eliteType, entity);


            ItemStack forceWeap = getWeaponType() != null ? AntiDuplication.getInstance().applyAntiDupe(new ItemGenerator()
                    .setType(net.dungeonrealms.game.world.item.Item.ItemType.getByName(getWeaponType()))
                    .setTier(net.dungeonrealms.game.world.item.Item.ItemTier.getByTier(tier))
                    .setRarity(GameAPI.getItemRarity(true)).generateItem().getItem()) : null;

            if (entity.getBukkitEntity() instanceof LivingEntity && forceWeap != null) {
                LivingEntity ent = (LivingEntity) entity.getBukkitEntity();

                ent.getEquipment().setItemInMainHand(forceWeap);
            }


            entity.setLocation(toSpawn.getX(), toSpawn.getY(), toSpawn.getZ(), 1, 1);
            world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
            entity.setLocation(toSpawn.getX(), toSpawn.getY(), toSpawn.getZ(), 1, 1);
            SPAWNED_MONSTERS.add(entity);

            if (this.getElementalDamage() != null) {
                if(ThreadLocalRandom.current().nextInt(100) <= this.getElementChance()) {
                    //Set the element ourselves.
                    GameAPI.setMobElement(entity, this.getElementalDamage());
                }
            }
            entity.getBukkitEntity().setMetadata("elite", new FixedMetadataValue(DungeonRealms.getInstance(), "true"));
            if (hasCustomName) {
                entity.setCustomName(GameAPI.getTierColor(tier) + ChatColor.BOLD.toString() + monsterCustomName.trim());
                entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), GameAPI.getTierColor(tier) + ChatColor.BOLD.toString() + monsterCustomName.trim()));
                entity.getBukkitEntity().setMetadata("namedElite", new FixedMetadataValue(DungeonRealms.getInstance(), GameAPI.getTierColor(tier) + ChatColor.BOLD.toString() + monsterCustomName.trim()));
            } else {
                entity.setCustomName(GameAPI.getTierColor(tier) + ChatColor.BOLD.toString() + monsterType.name.trim());
                entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), GameAPI.getTierColor(tier) + ChatColor.BOLD.toString() + monsterType.name.trim()));
            }
        }
    }

    /**
     * @param entity
     */

    private void giveCustomEquipment(EnumNamedElite eliteType, Entity entity) {
        EntityInsentient toGive = (EntityInsentient) entity;
        ItemStack[] armorWeapon = new ItemStack[5];
        switch (eliteType) {
            case MITSUKI:
                armorWeapon[0] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "sword");
                armorWeapon[1] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "boots");
                armorWeapon[2] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "legs");
                armorWeapon[3] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "chest");
                armorWeapon[4] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "helm");
                break;
            case COPJAK:
                armorWeapon[0] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "axe");
                armorWeapon[1] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "boot");
                armorWeapon[2] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "legs");
                armorWeapon[3] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "plate");
                armorWeapon[4] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "helm");
                break;
            case IMPATHEIMPALER:
                armorWeapon[0] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "polearm");
                armorWeapon[1] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "boots");
                armorWeapon[2] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "legs");
                armorWeapon[3] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "plate");
                armorWeapon[4] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "helm");
                break;
            case GREEDKING:
                armorWeapon[0] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "axe");
                armorWeapon[1] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "boots");
                armorWeapon[2] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "legs");
                armorWeapon[3] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "plate");
                armorWeapon[4] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "helm");
                break;
            case BLAYSHAN:
                armorWeapon[0] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "axe");
                armorWeapon[1] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "boots");
                armorWeapon[2] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "legs");
                armorWeapon[3] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "plate");
                armorWeapon[4] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "helm");
                break;
            case DURANOR:
                armorWeapon[0] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "halberd");
                armorWeapon[1] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "boots");
                armorWeapon[2] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "legs");
                armorWeapon[3] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "plate");
                armorWeapon[4] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "helm");
                break;
            case MOTHEROFDOOM:
                armorWeapon[0] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "Sword");
                armorWeapon[1] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "Boots");
                armorWeapon[2] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "Legs");
                armorWeapon[3] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "Plate");
                armorWeapon[4] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "Helm");
                break;
            case KILATAN:
                armorWeapon[0] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "staff");
                armorWeapon[1] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "boots");
                armorWeapon[2] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "legs");
                armorWeapon[3] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "plate");
                armorWeapon[4] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "helms");
                break;
            case ZION:
                armorWeapon[0] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "sword");
                armorWeapon[1] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "boots");
                armorWeapon[2] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "legs");
                armorWeapon[3] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "plate");
                armorWeapon[4] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "helms");
                break;
            case ACERON:
                break;
            case LORD_TAYLOR:
                armorWeapon[0] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "sword");
                armorWeapon[1] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "boots");
                armorWeapon[2] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "legs");
                armorWeapon[3] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "plate");
                armorWeapon[4] = ItemGenerator.getNamedItem(eliteType.getTemplateStarter() + "helms");
                break;
            default:
                break;
        }
        LivingEntity livingEntity = (LivingEntity) toGive.getBukkitEntity();
        if (eliteType != EnumNamedElite.NONE) {
            toGive.setEquipment(EnumItemSlot.MAINHAND, null);
            toGive.setEquipment(EnumItemSlot.FEET, null);
            toGive.setEquipment(EnumItemSlot.LEGS, null);
            toGive.setEquipment(EnumItemSlot.CHEST, null);
            toGive.setEquipment(EnumItemSlot.HEAD, null);
            livingEntity.getEquipment().setHelmet(null);
            livingEntity.getEquipment().setChestplate(null);
            livingEntity.getEquipment().setLeggings(null);
            livingEntity.getEquipment().setBoots(null);
            livingEntity.getEquipment().setItemInMainHand(null);
            for (int i = 0; i <= 4; i++) {
                if (armorWeapon[i] != null && armorWeapon[i].getType() != Material.AIR) {
                    EnchantmentAPI.addGlow(armorWeapon[i]);
                    switch (i) {
                        case 0:
                            livingEntity.getEquipment().setItemInMainHand(armorWeapon[i]);
                            toGive.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(armorWeapon[i]));
                            break;
                        case 1:
                            livingEntity.getEquipment().setBoots(armorWeapon[i]);
                            toGive.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armorWeapon[i]));
                            break;
                        case 2:
                            livingEntity.getEquipment().setLeggings(armorWeapon[i]);
                            toGive.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armorWeapon[i]));
                            break;
                        case 3:
                            livingEntity.getEquipment().setChestplate(armorWeapon[i]);
                            toGive.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armorWeapon[i]));
                            break;
                        case 4:
                            livingEntity.getEquipment().setHelmet(armorWeapon[i]);
                            toGive.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(armorWeapon[i]));
                            break;
                    }
                }
            }
            GameAPI.calculateAllAttributes(livingEntity, ((DRMonster) entity).getAttributes());
            entity.getBukkitEntity().setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), HealthHandler.getInstance().getMonsterMaxHPOnSpawn((LivingEntity) entity.getBukkitEntity())));
            HealthHandler.getInstance().setMonsterHPLive((LivingEntity) entity.getBukkitEntity(), HealthHandler.getInstance().getMonsterMaxHPLive((LivingEntity) entity.getBukkitEntity()));
        }
    }

    public void remove() {
        super.remove();
        isRemoved = true;
    }

    //Checks whether mobs can spawn based on their delay set in config.
    private boolean canMobsSpawn(boolean playersNearby) {
        if (counter < respawnDelay) {
            counter++;
            return false;
        } else {
            if (playersNearby) {
                counter = 0;
                return true;
            } else {
                counter = respawnDelay;
                return false;
            }
        }
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

    public Location getLocation() {
        return loc;
    }

    public EntityArmorStand getArmorstand() {
        return armorstand;
    }

    public int getTier() {
        return tier;
    }

    public List<Entity> getSPAWNED_MONSTERS() {
        return SPAWNED_MONSTERS;
    }

    public int getId() {
        return id;
    }

    public int getTimerID() {
        return timerID;
    }


    public String getCustomName() {
        return monsterCustomName;
    }

    public EnumNamedElite getEliteType() {
        return eliteType;
    }

    public EnumMonster getMonsterType() {
        return monsterType;
    }

}
