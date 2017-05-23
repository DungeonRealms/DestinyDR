package net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss;

import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mechanic.ReflectionAPI;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.base.DRGhast;
import net.dungeonrealms.game.world.entity.type.monster.boss.InfernalAbyss;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Infernal Ghast - Infernal Abyss subboss.
 * <p>
 * Redone on April 28th, 2017.
 *
 * @author Kneesnap
 */
public class InfernalGhast extends DRGhast implements DungeonBoss {

    public InfernalGhast(World world) {
        super(world);
        DamageAPI.setArmorBonus(getBukkit(), 50);

        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(50F);
        try {
            Field bSet = ReflectionAPI.getDeclaredField(goalSelector.getClass(), "b");
            LinkedHashSet<?> b = (LinkedHashSet) bSet.get(goalSelector);
            for (Object o : new LinkedHashSet<>(b)) {
                Field goal = ReflectionAPI.getDeclaredField(o.getClass(), "a");
                PathfinderGoal pathGoal = (PathfinderGoal) goal.get(o);
                if (pathGoal.getClass().getName().contains("GhastMoveTowardsTarget") || pathGoal.getClass().getName().contains("GhastAttackTarget")) {
                    //Remove this?
                    b.remove(o);
                    bSet.set(goalSelector, b);
                    Bukkit.getLogger().info("Removed ghast path finder: " + pathGoal.getClass().getName());
                }
            }
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
        this.goalSelector.a(7, new PathfinderGoalGhastAttackTarget(this));
        this.moveController = new ControllerGhast(this);
    }

    private InfernalAbyss getMainBoss() {
        return (InfernalAbyss) getDungeon().getBoss();
    }

    private Location instance;

    public void init() {
        HealthHandler.initHP(getBukkit(), getMainBoss().getHP());
        getBukkit().setPassenger(getMainBoss().getBukkit());
        instance = getSpawnPoint();
    }

    @Override
    public void n() {
        double distance = instance == null ? -1 : instance.distanceSquared(getBukkitEntity().getLocation());
        if (distance >= 100)
            getControllerMove().a(instance.getX(), instance.getY(), instance.getZ(), .5F);
        super.n();

        if (distance >= 100)
            getControllerMove().a(instance.getX(), instance.getY(), instance.getZ(), .5F);
    }

    public Location getSpawnPoint() {
        return new Location(getBukkitEntity().getWorld(), -53, 170, 660);
    }

    @Override
    public void onBossDeath(Player player) {
        playSound(Sound.ENTITY_GHAST_SCREAM, 2F, 1F);
    }

    @Override
    public void onBossAttacked(Player attacker) {
        if (getPercentHP() > 0.5F)
            return;

        // We're under 50% health, it's time to die and let infernal take over.
        getBukkit().eject();
        getMainBoss().doFinalForm(getHP());
        die();
    }

    @Override
    public BossType getBossType() {
        return BossType.InfernalGhast;
    }

    static class ControllerGhast extends ControllerMove {
        private EntityGhast i;
        private int j;

        public ControllerGhast(EntityGhast entityghast) {
            super(entityghast);
            this.i = entityghast;
        }

        public void c() {
            if (this.h == Operation.MOVE_TO) {
                double d0 = this.b - this.i.locX;
                double d1 = this.c - this.i.locY;
                double d2 = this.d - this.i.locZ;
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;
                if (this.j-- <= 0) {
                    this.j += this.i.getRandom().nextInt(5) + 2;
                    d3 = (double) MathHelper.sqrt(d3);
                    if (this.b(this.b, this.c, this.d, d3)) {
                        this.i.motX += d0 / d3 * 0.1D;
                        //Dont fly down?
                        this.i.motY += Math.max(-0.001, d1 / d3 * 0.1D);
                        this.i.motZ += d2 / d3 * 0.1D;
                    } else {
                        this.h = Operation.WAIT;
                    }
                }
            }

        }

        @Override
        public void a(double v, double v1, double v2, double v3) {
            super.a(v, v1, v2, v3);
        }

        private boolean b(double d0, double d1, double d2, double d3) {
            double d4 = (d0 - this.i.locX) / d3;
            double d5 = (d1 - this.i.locY) / d3;
            double d6 = (d2 - this.i.locZ) / d3;
            AxisAlignedBB axisalignedbb = this.i.getBoundingBox();

            for (int i = 1; (double) i < d3; ++i) {
                axisalignedbb = axisalignedbb.c(d4, d5, d6);
                List<AxisAlignedBB> cubes = this.i.world.getCubes(null, axisalignedbb);
                if (!cubes.isEmpty()) {
                    return false;
                }
            }

            return true;
        }
    }

    static class PathfinderGoalGhastAttackTarget extends PathfinderGoal {
        private EntityGhast ghast;
        public int a;

        public PathfinderGoalGhastAttackTarget(EntityGhast entityghast) {
            this.ghast = entityghast;
        }

        public boolean a() {
            return this.ghast.getGoalTarget() != null;
        }

        public void c() {
            this.a = 0;
        }

        public void d() {
            this.ghast.a(false);
        }

        public void e() {
            EntityLiving entityliving = this.ghast.getGoalTarget();
            double d0 = 64.0D;
            if (entityliving.h(this.ghast) < d0 * d0 && this.ghast.hasLineOfSight(entityliving)) {
                World world = this.ghast.world;
                ++this.a;
                if (this.a == 10) {
                    world.a((EntityHuman) null, 1015, new BlockPosition(this.ghast), 0);
                }

                if (this.a == 20) {
                    double d1 = 4.0D;
                    Vec3D vec3d = this.ghast.f(1.0F);
                    double d2 = entityliving.locX - (this.ghast.locX + vec3d.x * d1);
                    double d3 = entityliving.getBoundingBox().b + (double) (entityliving.length / 2.0F) - (0.5D + this.ghast.locY + (double) (this.ghast.length / 2.0F));
                    double d4 = entityliving.locZ - (this.ghast.locZ + vec3d.z * d1);
                    world.a((EntityHuman) null, 1016, new BlockPosition(this.ghast), 0);
                    //Custom fireball makes him shoot all insane?
                    EntityLargeFireball entitylargefireball = new EntityLargeFireball(world, this.ghast, d2, d3, d4) {
                        @Override
                        protected void a(MovingObjectPosition movingobjectposition) {
                            if (movingobjectposition.entity != null && (movingobjectposition.entity instanceof EntityFireball || !(movingobjectposition.entity instanceof EntityHuman) && !(shooter instanceof EntityHuman))) {
                                Bukkit.getLogger().info("Ignoring Fireball collision with " + movingobjectposition.entity);
                                return;
                            }
                            super.a(movingobjectposition);
                        }

                        @Override
                        public void setDirection(double d0, double d1, double d2) {
                            double d3 = (double) MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                            this.dirX = d0 / d3 * 0.1D;
                            this.dirY = d1 / d3 * 0.1D;
                            this.dirZ = d2 / d3 * 0.1D;
                        }

                    };
                    entitylargefireball.bukkitYield = (float) (entitylargefireball.yield = this.ghast.getPower());
                    entitylargefireball.locX = this.ghast.locX + vec3d.x * d1;
                    entitylargefireball.locY = this.ghast.locY + (double) (this.ghast.length / 2.0F) + 0.5D;
                    entitylargefireball.locZ = this.ghast.locZ + vec3d.z * d1;
                    world.addEntity(entitylargefireball);
                    this.a = -40;
                }
            } else if (this.a > 0) {
                --this.a;
            }

            this.ghast.a(this.a > 10);
        }
    }

}
