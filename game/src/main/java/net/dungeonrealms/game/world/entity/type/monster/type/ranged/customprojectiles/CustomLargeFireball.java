package net.dungeonrealms.game.world.entity.type.monster.type.ranged.customprojectiles;

import net.minecraft.server.v1_9_R2.DamageSource;
import net.minecraft.server.v1_9_R2.EntityLargeFireball;
import net.minecraft.server.v1_9_R2.MovingObjectPosition;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;

public class CustomLargeFireball extends EntityLargeFireball implements CustomProjectileFireball {
    private double accuracy = 1;

    public CustomLargeFireball(World world, CraftLivingEntity shooter, double x, double y, double z, double accuracy) {
        super(world);
        this.setPositionRotation(shooter.getLocation().getX(), shooter.getLocation().getY(), shooter.getLocation().getZ(), shooter.getLocation().getYaw(), shooter.getLocation().getPitch());
        this.setPosition(locX, locY, locZ);
        this.motX = this.motY = this.motZ = 0.0D;
        this.accuracy = accuracy;
        this.shooter = shooter.getHandle();
        this.projectileSource = shooter;
        setDirection(x, y, z);
    }

    @Override
    protected void a(MovingObjectPosition movingobjectposition) {
        if (this.onCollision(movingobjectposition, shooter))
            super.a(movingobjectposition);
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
