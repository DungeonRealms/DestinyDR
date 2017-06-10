package net.dungeonrealms.game.world.entity.type.monster.type.ranged.customprojectiles;

import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.EntityTippedArrow;
import net.minecraft.server.v1_9_R2.MovingObjectPosition;
import net.minecraft.server.v1_9_R2.World;

public class CustomEntityTippedArrow extends EntityTippedArrow implements CustomProjectile {
    public CustomEntityTippedArrow(World world, EntityLiving shooter) {
        super(world, shooter);
    }

    @Override
    protected void a(MovingObjectPosition movingobjectposition) {
        if (onCollision(movingobjectposition, this.shooter)) {
            super.a(movingobjectposition);
        }
    }
}
