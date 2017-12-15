package net.dungeonrealms.game.world.entity.type.monster.type.ranged.customprojectiles;

import net.minecraft.server.v1_9_R2.*;

public class CustomEntitySpectralArrow extends EntitySpectralArrow implements CustomProjectile {

    public CustomEntitySpectralArrow(World world, EntityLiving shooter) {
        super(world, shooter);
    }

    @Override
    protected void a(MovingObjectPosition movingobjectposition) {
        if (onCollision(movingobjectposition, this.shooter)) {
            super.a(movingobjectposition);
        }
    }
}

