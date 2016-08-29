package net.dungeonrealms.game.world.entity;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.NMSUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.entity.powermove.PowerMove;
import net.dungeonrealms.game.world.entity.type.monster.base.*;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.Burick;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.InfernalAbyss;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.Mayel;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.InfernalGhast;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.InfernalLordsGuard;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.*;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.RangedSkeleton;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.RangedWitherSkeleton;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.RangedZombie;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff.BasicEntityBlaze;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff.StaffSkeleton;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff.StaffZombie;
import net.dungeonrealms.game.world.entity.type.mounts.EnderDragon;
import net.dungeonrealms.game.world.entity.type.mounts.Horse;
import net.dungeonrealms.game.world.entity.type.pet.*;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Kieran on 9/18/2015.
 */
public class EntityMechanics implements GenericMechanic {

    private static EntityMechanics instance = null;
    public static HashMap<UUID, Entity> PLAYER_PETS = new HashMap<>();
    public static HashMap<UUID, Entity> PLAYER_MOUNTS = new HashMap<>();
    public static ConcurrentHashMap<LivingEntity, Integer> MONSTER_LAST_ATTACK = new ConcurrentHashMap<>();
    public static CopyOnWriteArrayList<LivingEntity> MONSTERS_LEASHED = new CopyOnWriteArrayList<>();

    public static EntityMechanics getInstance() {
        if (instance == null) {
            return new EntityMechanics();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
    public void startInitialization() {
        NMSUtils nmsUtils = new NMSUtils();

        // Monsters
        //MELEE MONSTERS
        nmsUtils.registerEntity("MeleeGolem", 99, EntityGolem.class, MeleeGolem.class);
        nmsUtils.registerEntity("LargeSpider", 52, EntitySpider.class, LargeSpider.class);
        nmsUtils.registerEntity("SmallSpider",59, EntityCaveSpider.class, SmallSpider.class);
        nmsUtils.registerEntity("MeleeZombie", 54, EntityZombie.class, MeleeZombie.class);
        nmsUtils.registerEntity("MeleeWitherSkeleton", 51, EntitySkeleton.class, MeleeWitherSkeleton.class);
        nmsUtils.registerEntity("MeleeSkeleton", 51, EntitySkeleton.class, MeleeSkeleton.class);
        nmsUtils.registerEntity("MeleeEnderman", 58, EntityEnderman.class, MeleeEnderman.class);
        //STAFF MONSTERS
        nmsUtils.registerEntity("BasicBlaze", 61, EntityBlaze.class, BasicEntityBlaze.class);
        nmsUtils.registerEntity("StaffZombie", 54, EntityZombie.class, StaffZombie.class);
        nmsUtils.registerEntity("StaffSkeleton", 51, EntitySkeleton.class, StaffSkeleton.class);
        //BOW MONSTERS
        nmsUtils.registerEntity("RangedSkeleton", 51, EntitySkeleton.class, RangedSkeleton.class);
        nmsUtils.registerEntity("RangedZombie", 54, EntityZombie.class, RangedZombie.class);
        nmsUtils.registerEntity("RangedWitherSkeleton", 51, EntitySkeleton.class, RangedWitherSkeleton.class);
        //BASE MONSTERS
        nmsUtils.registerEntity("DRSpider", 59, EntitySpider.class, DRSpider.class);
        nmsUtils.registerEntity("DRWither", 51, EntitySkeleton.class, DRWitherSkeleton.class);
        nmsUtils.registerEntity("DRBlaze", 61, EntityBlaze.class, DRBlaze.class);
        nmsUtils.registerEntity("DRMagma", 62, EntityMagmaCube.class, DRMagma.class);
        nmsUtils.registerEntity("DRPigman", 57, EntityPigZombie.class, DRPigman.class);
        nmsUtils.registerEntity("DRSilverfish", 60, EntitySilverfish.class, DRSilverfish.class);
        nmsUtils.registerEntity("DRWolf", 95, EntityWolf.class, DRWolf.class);
        nmsUtils.registerEntity("DRWitch", 66, EntityWitch.class, DRWitch.class);
        nmsUtils.registerEntity("DRZombie", 54, EntityZombie.class, DRZombie.class);
        nmsUtils.registerEntity("DRGolem", 99, EntityGolem.class, DRGolem.class);
        nmsUtils.registerEntity("DREnderman", 58, EntityEnderman.class, DREnderman.class);

        // Tier 1 Boss
        nmsUtils.registerEntity("Mayel", 51, EntitySkeleton.class, Mayel.class);

        // Tier 3 Boss
        nmsUtils.registerEntity("Burick", 51, EntitySkeleton.class, Burick.class);

        // Tier 4 Boss
        nmsUtils.registerEntity("InfernalAbyss", 51, EntitySkeleton.class, InfernalAbyss.class);

        // Tier 4 Sub-bosses
        nmsUtils.registerEntity("DRGhast", 56, EntityGhast.class, InfernalGhast.class);
        nmsUtils.registerEntity("LordsGuard", 51, EntitySkeleton.class, InfernalLordsGuard.class);

        // Pets
        nmsUtils.registerEntity("PetCaveSpider", 59, EntityCaveSpider.class, CaveSpider.class);
        nmsUtils.registerEntity("PetBabyZombie", 54, EntityZombie.class, BabyZombie.class);
        nmsUtils.registerEntity("PetBabyZombiePig", 57, EntityPigZombie.class, BabyZombiePig.class);
        nmsUtils.registerEntity("PetWolf", 95, EntityWolf.class, Wolf.class);
        nmsUtils.registerEntity("PetChicken", 93, EntityChicken.class, Chicken.class);
        nmsUtils.registerEntity("PetOcelot", 98, EntityOcelot.class, Ocelot.class);
        nmsUtils.registerEntity("PetRabbit", 101, EntityRabbit.class, Rabbit.class);
        nmsUtils.registerEntity("PetSilverfish", 60, EntitySilverfish.class, Silverfish.class);
        nmsUtils.registerEntity("PetEndermite", 67, EntityEndermite.class, Endermite.class);
        nmsUtils.registerEntity("MountHorse", 100, EntityHorse.class, Horse.class);
        nmsUtils.registerEntity("PetSnowman", 97, EntitySnowman.class, Snowman.class);
        nmsUtils.registerEntity("MountEnderDragon", 63, EntityEnderDragon.class, EnderDragon.class);
        nmsUtils.registerEntity("PetBat", 65, EntityBat.class, Bat.class);
        nmsUtils.registerEntity("PetSlime", 55, EntitySlime.class, Slime.class);
        nmsUtils.registerEntity("PetMagmaCube", 62, EntityMagmaCube.class, MagmaCube.class);
        nmsUtils.registerEntity("PetCreeper", 50, EntityCreeper.class, Creeper.class);

        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::checkForLeashedMobs, 0, 20L);
    }

    @Override
    public void stopInvocation() {

    }

    private void checkForLeashedMobs() {
        if (!MONSTERS_LEASHED.isEmpty()) {
            for (LivingEntity entity : MONSTERS_LEASHED) {
                if (entity == null) {
                    Utils.log.warning("[ENTITIES] [ASYNC] Mob is somehow leashed but null, safety removing!");
                    continue;
                }
                if (entity.isDead()) {
                    MONSTERS_LEASHED.remove(entity);
                    if (MONSTER_LAST_ATTACK.containsKey(entity)) {
                        MONSTER_LAST_ATTACK.remove(entity);
                    }
                    entity.remove();
                    continue;
                }
                if (entity.hasMetadata("dungeon") || entity.hasMetadata("boss")) {
                    MONSTERS_LEASHED.remove(entity);
                    if (MONSTER_LAST_ATTACK.containsKey(entity)) {
                        MONSTER_LAST_ATTACK.remove(entity);
                    }
                    continue;
                }
                if (MONSTER_LAST_ATTACK.containsKey(entity)) {
                    if (MONSTER_LAST_ATTACK.get(entity) == 11) {
                        EntityInsentient entityInsentient = (EntityInsentient) ((CraftEntity) entity).getHandle();
                        if (entityInsentient != null && entityInsentient.getGoalTarget() != null) {
                            if (entityInsentient.getGoalTarget().getBukkitEntity().getLocation().distance(entity.getLocation()) >= 2 && entityInsentient.getGoalTarget().getBukkitEntity().getLocation().distance(entity.getLocation()) <= 6) {
                                if (entityInsentient.getGoalTarget().getBukkitEntity().getLocation().getBlockY() != entity.getLocation().getBlockY()) {
                                    Location loc = entityInsentient.getGoalTarget().getBukkitEntity().getLocation();
                                    ((CraftEntity) entity).getHandle().setLocation(loc.getX(), loc.getY() + 1, loc.getZ(), loc.getYaw(), loc.getPitch());
                                    MONSTER_LAST_ATTACK.put(entity, 15);
                                }
                            }
                        }
                    } else if (MONSTER_LAST_ATTACK.get(entity) == 10) {
                        if (entity.hasMetadata("elite")) {
                            if (entity.hasMetadata("namedElite")) {
                                entity.setCustomName(entity.getMetadata("namedElite").get(0).asString().trim());
                            } else if (entity.hasMetadata("customname")) {
                                entity.setCustomName(entity.getMetadata("customname").get(0).asString().trim());
                            }
                        } else {
                            String lvlName = ChatColor.AQUA.toString() + "[Lvl. " + entity.getMetadata("level").get(0).asInt() + "] " + ChatColor.RESET;
                            if (entity.hasMetadata("customname")) {
                                entity.setCustomName(lvlName + entity.getMetadata("customname").get(0).asString());
                            }
                        }
                        entity.setCustomNameVisible(true);
                    } else if (MONSTER_LAST_ATTACK.get(entity) <= 0) {
                        MONSTERS_LEASHED.remove(entity);
                        MONSTER_LAST_ATTACK.remove(entity);
                        tryToReturnMobToBase(((CraftEntity) entity).getHandle());
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            ((EntityInsentient) ((CraftEntity) entity).getHandle()).setGoalTarget(null, EntityTargetEvent.TargetReason.CUSTOM, true);
                        }, 220L);
                        continue;
                    }
                    MONSTER_LAST_ATTACK.put(entity, (MONSTER_LAST_ATTACK.get(entity) - 1));
                } else {
                    MONSTER_LAST_ATTACK.put(entity, 15);
                }
            }
        }
    }

    private void tryToReturnMobToBase(Entity entity) {
        SpawningMechanics.getALLSPAWNERS().stream().filter(mobSpawner -> mobSpawner.getSpawnedMonsters().contains(entity))
                .forEach(mobSpawner -> {
                    EntityInsentient entityInsentient = (EntityInsentient) entity;
                    entityInsentient.setGoalTarget(mobSpawner.getArmorstand(), EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
                    PathEntity path = entityInsentient.getNavigation().a(mobSpawner.getArmorstand().locX, mobSpawner.getArmorstand().locY, mobSpawner.getArmorstand().locZ);
                    entityInsentient.getNavigation().a(path, 2);
                    double distance = mobSpawner.getArmorstand().getBukkitEntity().getLocation().distance(entity.getBukkitEntity().getLocation());
                    if (distance > 30 && !entity.dead) {
                        entity.getBukkitEntity().teleport(mobSpawner.getArmorstand().getBukkitEntity().getLocation());
                        entityInsentient.setGoalTarget(mobSpawner.getArmorstand(), EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
                    }
                });
    }

    public int getBarLength(int tier) {
        if (tier == 1) {
            return 25;
        }
        if (tier == 2) {
            return 30;
        }
        if (tier == 3) {
            return 35;
        }
        if (tier == 4) {
            return 40;
        }
        if (tier == 5) {
            return 50;
        }
        return 25;
    }

    public String generateOverheadBar(org.bukkit.entity.Entity ent, double cur_hp, double max_hp, int tier, boolean elite) {
        int max_bar = getBarLength(tier);

        boolean boss = ent.hasMetadata("boss");
        ChatColor cc;

        DecimalFormat df = new DecimalFormat("##.#");
        double percent_hp = (double) (Math.round(100.0D * Double.parseDouble((df.format((cur_hp / max_hp)))))); // EX: 0.5054134131

        if (percent_hp <= 0 && cur_hp > 0) {
            percent_hp = 1;
        }

        cc = ChatColor.GREEN;

        if (boss) {
            max_bar = 60;
            cc = ChatColor.GOLD;
        }

        double percent_interval = (100.0D / max_bar);
        int bar_count = 0;

        if (percent_hp <= 45) {
            cc = ChatColor.YELLOW;
        }
        if (percent_hp <= 20) {
            cc = ChatColor.RED;
        }


        if(PowerMove.chargingMonsters.contains(ent.getUniqueId()) || PowerMove.chargedMonsters.contains(ent.getUniqueId())){
            cc = ChatColor.LIGHT_PURPLE;
        }

        String return_string = cc + ChatColor.BOLD.toString() + "║" + ChatColor.RESET.toString() + cc.toString() + "";
        if (elite || boss) {
            return_string += ChatColor.BOLD.toString();
        }

        while (percent_hp > 0 && bar_count < max_bar) {
            percent_hp -= percent_interval;
            bar_count++;
            return_string += "|";
        }

        return_string += ChatColor.BLACK.toString();

        if (elite) {
            return_string += ChatColor.BOLD.toString();
        }

        while (bar_count < max_bar) {
            return_string += "|";
            bar_count++;
        }

        return_string = return_string + cc + ChatColor.BOLD.toString() + "║";
        if (!elite && !boss) {
            return return_string + ChatColor.AQUA.toString() + "[Lvl. " + ent.getMetadata("level").get(0).asInt() + "]";
        } else {
            return return_string;
            // 20 Bars, that's 5% HP per bar
        }
    }
}

