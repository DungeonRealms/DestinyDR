package net.dungeonrealms.game.world.entity.type.monster.type.ranged.customprojectiles;

import net.minecraft.server.v1_9_R2.DamageSource;
import net.minecraft.server.v1_9_R2.EntityDragonFireball;
import net.minecraft.server.v1_9_R2.MovingObjectPosition;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Explosive;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class CustomDragonFireball extends EntityDragonFireball implements CustomProjectileFireball {

    double accuracy = 1;

    public CustomDragonFireball(World world, CraftLivingEntity shooter, double x, double y, double z, double accuracy) {
        super(world);
        this.setPositionRotation(shooter.getLocation().getX(), shooter.getLocation().getY(), shooter.getLocation().getZ(), shooter.getLocation().getYaw(), shooter.getLocation().getPitch());
        this.motX = this.motY = this.motZ = 0.0D;
        this.accuracy = accuracy;
        this.shooter = shooter.getHandle();
        this.projectileSource = shooter;
        setDirection(x, y, z);
    }

    @Override
    protected void a(MovingObjectPosition movingobjectposition) {
        if (onCollision(movingobjectposition, shooter)) {
            if (!this.world.isClientSide) {
                if (movingobjectposition.entity != null) {
                    movingobjectposition.entity.damageEntity(DamageSource.fireball(this, this.shooter), 6.0F);
                    this.a(this.shooter, movingobjectposition.entity);
                }

                boolean flag = this.world.getGameRules().getBoolean("mobGriefing");
                ExplosionPrimeEvent event = new ExplosionPrimeEvent((Explosive) CraftEntity.getEntity(this.world.getServer(), this));
                this.world.getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    this.world.createExplosion(this, this.locX, this.locY, this.locZ, event.getRadius(), event.getFire(), flag);
                }

                this.die();
            }
        }
    }


    @Override
    public void setDirection(double d0, double d1, double d2) {
        double[] newDirection = attemptSetDirection(d0, d1, d2, accuracy);

        this.dirX = newDirection[0];
        this.dirY = newDirection[1];
        this.dirZ = newDirection[2];
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        return damageEntity(damagesource, this, f);
    }
}
