package net.dungeonrealms.game.world.entity;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.NMSUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster.CustomEntityType;
import net.dungeonrealms.game.world.entity.type.mounts.*;
import net.dungeonrealms.game.world.entity.type.pet.*;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.*;
import net.minecraft.server.v1_9_R2.Entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.event.CraftEventFactory;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.util.Vector;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Kieran on 9/18/2015.
 */
public class EntityMechanics implements GenericMechanic {

	@Getter
    private static EntityMechanics instance = new EntityMechanics();
    
    public static ConcurrentHashMap<LivingEntity, Integer> MONSTER_LAST_ATTACK = new ConcurrentHashMap<>();
    public static CopyOnWriteArrayList<LivingEntity> MONSTERS_LEASHED = new CopyOnWriteArrayList<>();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

	@Override
    public void startInitialization() {
    	
    	//  REGISTER MONSTERS  //
    	for (CustomEntityType type : CustomEntityType.values())
    		type.register();
        
        //  REGISTER PETS  //
        for (EnumPets pet : EnumPets.values())
        	if (!pet.isFrame())
        		NMSUtils.registerEntity(pet.getClazz().getSimpleName(), pet.getEggShortData(), pet.getClazz());
        
        //  REGISTER MOUNTS  //
        for (EnumMounts m : EnumMounts.values())
        	if (m.shouldRegister())
        		NMSUtils.registerEntity(m.getClazz().getSimpleName(), m.getEntityId(), m.getClazz());

        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::checkForLeashedMobs, 0, 20L);
    }

    @Override
    public void stopInvocation() {
    	
    }

    public static Projectile spawnFireballProjectile(World world, CraftLivingEntity shooter, Vector velocity, Class<? extends Fireball> projectile, double accuracy) {
        Location location = shooter.getEyeLocation();
        Vector direction = location.getDirection().multiply(10);

        Entity launch = null;
        double accurate = .4D - (.4D * (accuracy / 100));
        if (Fireball.class.isAssignableFrom(projectile)) {
            if (SmallFireball.class.isAssignableFrom(projectile)) {
                launch = new EntitySmallFireball(world, shooter.getHandle(), direction.getX(), direction.getY(), direction.getZ()) {
                    @Override
                    public void setDirection(double d0, double d1, double d2) {
                        d0 += this.random.nextGaussian() * accurate;
                        d1 += this.random.nextGaussian() * accurate;
                        d2 += this.random.nextGaussian() * accurate;
                        double d3 = (double) MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        this.dirX = d0 / d3 * 0.1D;
                        this.dirY = d1 / d3 * 0.1D;
                        this.dirZ = d2 / d3 * 0.1D;
                    }

                    @Override
                    public boolean damageEntity(DamageSource damagesource, float f) {
                        if (this.isInvulnerable(damagesource))
                            return false;
                        this.ao();
                        return damagesource.getEntity() != null ? !CraftEventFactory.handleNonLivingEntityDamageEvent(this, damagesource, (double) f) : false;
                    }
                };
            } else if (WitherSkull.class.isAssignableFrom(projectile)) {
                //Pending
                launch = new EntityWitherSkull(world, shooter.getHandle(), direction.getX(), direction.getY(), direction.getZ()) {
                    @Override
                    public void setDirection(double d0, double d1, double d2) {
                        d0 += this.random.nextGaussian() * accurate;
                        d1 += this.random.nextGaussian() * accurate;
                        d2 += this.random.nextGaussian() * accurate;
                        double d3 = (double) MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        this.dirX = d0 / d3 * 0.1D;
                        this.dirY = d1 / d3 * 0.1D;
                        this.dirZ = d2 / d3 * 0.1D;
                    }
                };
            } else if (DragonFireball.class.isAssignableFrom(projectile)) {
                launch = new EntityDragonFireball(world, shooter.getHandle(), direction.getX(), direction.getY(), direction.getZ());
            } else {
                launch = new EntityLargeFireball(world, shooter.getHandle(), direction.getX(), direction.getY(), direction.getZ()) {
                    @Override
                    public void setDirection(double d0, double d1, double d2) {
                        d0 += this.random.nextGaussian() * accurate;
                        d1 += this.random.nextGaussian() * accurate;
                        d2 += this.random.nextGaussian() * accurate;
                        //Dont add any randomness too it since its meant to be accurateish
                        double d3 = (double) MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        this.dirX = d0 / d3 * 0.1D;
                        this.dirY = d1 / d3 * 0.1D;
                        this.dirZ = d2 / d3 * 0.1D;
                    }

                    @Override
                    public boolean damageEntity(DamageSource damagesource, float f) {
                        if (this.isInvulnerable(damagesource))
                            return false;
                        this.ao();
                        return damagesource.getEntity() != null ? !CraftEventFactory.handleNonLivingEntityDamageEvent(this, damagesource, (double) f) : false;
                    }
                };
            }
            ((EntityFireball) launch).projectileSource = shooter;
        }

        if (launch == null)
        	return null;
        
        launch.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        if (velocity != null)
            launch.getBukkitEntity().setVelocity(velocity);

        world.addEntity(launch);
        return (Projectile) launch.getBukkitEntity();
    }

    public static void setVelocity(Player player, Vector velocity) {

        if (Double.isNaN(velocity.getX()) || Double.isNaN(velocity.getY()) || Double.isNaN(velocity.getZ())) {
            Bukkit.getLogger().info("Prevented Crash due to velocity: " + velocity + " bound for " + player.getName() + " at " + player.getLocation().toString());
            //Get the source of the problem.
            try {
                Thread.dumpStack();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        player.setVelocity(velocity);
    }

    /**
     * Handles general restrictions on entities.
     * Could be named better.
     */
    private void checkForLeashedMobs() {
    	for (LivingEntity entity : MONSTERS_LEASHED) {
    		if (entity == null) {
    			Utils.log.warning("[ENTITIES] [ASYNC] Mob is somehow leashed but null, safety removing!");
    			continue;
    		}
    		if (entity.isDead() || Metadata.DUNGEON.get(entity).asBoolean() || Metadata.BOSS.get(entity).asBoolean()) {
    			MONSTERS_LEASHED.remove(entity);
    			MONSTER_LAST_ATTACK.remove(entity);
    			if (entity.isDead()) //Remove the entity if it's dead...
    				entity.remove();
    			continue;
    		}
    		if (!MONSTER_LAST_ATTACK.containsKey(entity)) {
    			MONSTER_LAST_ATTACK.put(entity, 15);
    			continue;
    		}
    		
    		int lastAttack = MONSTER_LAST_ATTACK.get(entity);
    		EntityInsentient ei = (EntityInsentient) ((CraftEntity)entity).getHandle();
    		
    		if (lastAttack == 11) {
    			// Teleport back to spawnpoint if too far away.
    			Location target = ei.getGoalTarget().getBukkitEntity().getLocation();
    			if (target != null && target.getWorld() == entity.getWorld()) {
    				double distance = target.distance(entity.getLocation());
    				
    				// If they're a certain range away from the player and on a different Y level, they could be safe-spotting.
    				if (distance >= 2 && distance <= 6 && target.getBlockY() != entity.getLocation().getBlockY()) {
    					entity.teleport(target);
    					MONSTER_LAST_ATTACK.put(entity, 15);
    				}
    			}
    		} else if (lastAttack == 10) {
    			// Update entity name.
    			EntityAPI.updateName(entity);
    		} else if (lastAttack <= 0){
    			// Remove.
    			MONSTERS_LEASHED.remove(entity);
    			MONSTER_LAST_ATTACK.remove(entity);
    			tryToReturnMobToBase(((CraftEntity) entity).getHandle());
    			// Reset goal.
    			Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> ei.setGoalTarget(null, TargetReason.CUSTOM, true), 220L);
    		}
    		
    		MONSTER_LAST_ATTACK.put(entity, lastAttack - 1);
    	}
    }

    private void tryToReturnMobToBase(Entity entity) {
        SpawningMechanics.getSpawners().stream().filter(mobSpawner -> mobSpawner.getSpawnedMonsters().contains(entity))
                .forEach(mobSpawner -> {
                	EntityArmorStand eas = (EntityArmorStand) ((CraftEntity)mobSpawner.getArmorStand()).getHandle();
                    EntityInsentient entityInsentient = (EntityInsentient) entity;
                    entityInsentient.setGoalTarget(eas, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
                    Location l = mobSpawner.getLocation();
                    PathEntity path = entityInsentient.getNavigation().a(l.getX(), l.getY(), l.getZ());
                    entityInsentient.getNavigation().a(path, 2);
                    double distance = mobSpawner.getArmorStand().getLocation().distance(entity.getBukkitEntity().getLocation());
                    if (distance > 30 && !entity.dead) {
                        entity.getBukkitEntity().teleport(mobSpawner.getArmorStand().getLocation());
                        entityInsentient.setGoalTarget(eas, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true);
                    }
                });
    }
}

