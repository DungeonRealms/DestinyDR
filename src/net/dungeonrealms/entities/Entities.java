package net.dungeonrealms.entities;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.types.RangedEntityBlaze;
import net.dungeonrealms.entities.types.monsters.*;
import net.dungeonrealms.entities.types.monsters.EntityGolem;
import net.dungeonrealms.entities.types.monsters.EntitySpider;
import net.dungeonrealms.entities.types.monsters.boss.Burick;
import net.dungeonrealms.entities.types.monsters.boss.InfernalAbyss;
import net.dungeonrealms.entities.types.monsters.boss.InfernalGhast;
import net.dungeonrealms.entities.types.monsters.boss.Mayel;
import net.dungeonrealms.entities.types.monsters.boss.subboss.InfernalLordsGuard;
import net.dungeonrealms.entities.types.monsters.boss.subboss.Pyromancer;
import net.dungeonrealms.entities.types.mounts.EnderDragon;
import net.dungeonrealms.entities.types.mounts.Horse;
import net.dungeonrealms.entities.types.pets.*;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.mastery.NMSUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.spawning.SpawningMechanics;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Kieran on 9/18/2015.
 */
public class Entities implements GenericMechanic{

    private static Entities instance = null;
    public static HashMap<UUID, Entity> PLAYER_PETS = new HashMap<>();
    public static HashMap<UUID, Entity> PLAYER_MOUNTS = new HashMap<>();
    public ConcurrentHashMap<LivingEntity, Integer> MONSTER_LAST_ATTACK = new ConcurrentHashMap<>();
    public List<LivingEntity> MONSTERS_LEASHED = new CopyOnWriteArrayList<>();

    public static Entities getInstance() {
        if (instance == null) {
            return new Entities();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    public void startInitialization() {
        NMSUtils nmsUtils = new NMSUtils();

        //Monsters
        nmsUtils.registerEntity("Pirate", 54, EntityZombie.class, EntityPirate.class);
        nmsUtils.registerEntity("RangedPirate", 54, EntityZombie.class, EntityRangedPirate.class);
        nmsUtils.registerEntity("Fire Imp", 54, EntityZombie.class, EntityFireImp.class);
        nmsUtils.registerEntity("Bandit", 51, EntitySkeleton.class, EntityBandit.class);
        nmsUtils.registerEntity("Enchanted Golem", 99, net.minecraft.server.v1_8_R3.EntityGolem.class, EntityGolem.class);
        nmsUtils.registerEntity("DR Spider", 59, net.minecraft.server.v1_8_R3.EntitySpider.class, EntitySpider.class);
        nmsUtils.registerEntity("CustomEntity", 54, EntityZombie.class, BasicMeleeMonster.class);
        nmsUtils.registerEntity("BasicMage", 54, EntityZombie.class, BasicMageMonster.class);
        nmsUtils.registerEntity("DRWither", 51, EntitySkeleton.class, EntityWitherSkeleton.class);
        nmsUtils.registerEntity("DRBlaze", 61, EntityBlaze.class, RangedEntityBlaze.class);
        nmsUtils.registerEntity("DRSkeleton", 51, EntitySkeleton.class, BasicEntitySkeleton.class);
        nmsUtils.registerEntity("DRMagma", 62, EntityMagmaCube.class, BasicEntityMagma.class);
        nmsUtils.registerEntity("DRPigman", 57, EntityPigZombie.class, BasicEntityPigman.class);
        nmsUtils.registerEntity("DRSilverfish", 60, EntitySilverfish.class, BasicEntitySilverfish.class);
        
        //Tier 1 Boss
        nmsUtils.registerEntity("Mayel", 51, EntitySkeleton.class, Mayel.class);
        nmsUtils.registerEntity("Pyromancer", 51, EntitySkeleton.class, Pyromancer.class);

        //Tier 3 Boss
        nmsUtils.registerEntity("Burick", 51, EntitySkeleton.class, Burick.class);
        
        //Tier 4 Boss
        nmsUtils.registerEntity("InfernalAbyss", 51, EntitySkeleton.class, InfernalAbyss.class);
        nmsUtils.registerEntity("DRGhast", 56, EntityGhast.class, InfernalGhast.class);
        nmsUtils.registerEntity("LordsGuard", 51, EntitySkeleton.class, InfernalLordsGuard.class);

        //Pets
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

        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::checkForLeashedMobs, 10, 20L);
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
                if (MONSTER_LAST_ATTACK.containsKey(entity)) {
                    if (MONSTER_LAST_ATTACK.get(entity) <= 0) {
                        MONSTERS_LEASHED.remove(entity);
                        MONSTER_LAST_ATTACK.remove(entity);
                        tryToReturnMobToBase(((CraftEntity) entity).getHandle());
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
                        int level = entity.getMetadata("level").get(0).asInt();
                        String name = ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] " + ((net.dungeonrealms.entities.Monster)entity).getEnum().name;
                      	if(!entity.isDead())
                          entity.setCustomName(name);

                      }, 100);
                        int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                            if (HealthHandler.getInstance().getMonsterHPLive(entity) < HealthHandler.getInstance().getMonsterMaxHPLive(entity) && !MONSTERS_LEASHED.contains(entity) && !MONSTER_LAST_ATTACK.containsKey(entity)) {
                            	Utils.log.info("Healed " + entity.getCustomName());
                                HealthHandler.getInstance().healMonsterByAmount(entity, (HealthHandler.getInstance().getMonsterMaxHPLive(entity) / 10));
                                //TODO: Inform nearby players it is being healed -- Message or Particle effect?
                            }
                        }, 0L, 20L);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            Bukkit.getScheduler().cancelTask(taskID);
                            ((EntityInsentient) ((CraftEntity) entity).getHandle()).setGoalTarget(null, EntityTargetEvent.TargetReason.CUSTOM, true);
                        }, 220L);
                    } else {
                        MONSTER_LAST_ATTACK.put(entity, (MONSTER_LAST_ATTACK.get(entity) - 1));
                    }
                } else {
                    MONSTER_LAST_ATTACK.put(entity, 15);
                }
            }
        }
    }

    private void tryToReturnMobToBase(Entity entity) {
        SpawningMechanics.getSpawners().stream().filter(mobSpawner -> mobSpawner.getSpawnedMonsters().contains(entity)).forEach(mobSpawner -> {
            EntityInsentient entityInsentient = (EntityInsentient) entity;
            entityInsentient.setGoalTarget(mobSpawner.armorstand, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
            PathEntity path = entityInsentient.getNavigation().a(mobSpawner.armorstand.locX, mobSpawner.armorstand.locY, mobSpawner.armorstand.locZ);
            entityInsentient.getNavigation().a(path, 2);
            double distance = mobSpawner.armorstand.getBukkitEntity().getLocation().distance(entity.getBukkitEntity().getLocation());
            if (distance > 30 && !entity.dead) {
                entity.getBukkitEntity().teleport(mobSpawner.armorstand.getBukkitEntity().getLocation());
                entityInsentient.setGoalTarget(mobSpawner.armorstand, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
            }
        });
    }
}
