package net.dungeonrealms.game.world.entity.type.monster.type.ranged.customprojectiles;

import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.event.CraftEventFactory;

public interface CustomProjectileFireball extends CustomProjectile {

    @Override
    default boolean onCollision(MovingObjectPosition movingobjectposition, Entity shooter) {
        if (movingobjectposition.entity != null && (movingobjectposition.entity instanceof EntityFireball || !(movingobjectposition.entity instanceof EntityHuman) && !(shooter instanceof EntityHuman))) {
            Bukkit.getLogger().info("Ignoring Fireball collision with " + movingobjectposition.entity);
            return false;
        }
        return true;
    }

    default boolean damageEntity(DamageSource damagesource, EntityFireball ball, float f) {
        if (ball.isInvulnerable(damagesource))
            return false;
        return damagesource.getEntity() != null && !CraftEventFactory.handleNonLivingEntityDamageEvent(ball, damagesource, (double) f);
    }
}
