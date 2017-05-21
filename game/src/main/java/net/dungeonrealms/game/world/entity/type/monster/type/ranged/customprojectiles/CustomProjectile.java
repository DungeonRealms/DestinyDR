package net.dungeonrealms.game.world.entity.type.monster.type.ranged.customprojectiles;

import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.MathHelper;
import net.minecraft.server.v1_9_R2.MovingObjectPosition;
import org.bukkit.Bukkit;

import java.util.concurrent.ThreadLocalRandom;

public interface CustomProjectile {
    default boolean onCollision(MovingObjectPosition movingobjectposition, Entity shooter) {
        if (movingobjectposition.entity != null && !(movingobjectposition.entity instanceof EntityHuman) && !(shooter instanceof EntityHuman)) {
            return false;
        }
        return true;
    }

    default double[] attemptSetDirection(double d0, double d1, double d2, double accurate) {
        double[] retr = new double[3];
        d0 += ThreadLocalRandom.current().nextGaussian() * accurate;
        d1 += ThreadLocalRandom.current().nextGaussian() * accurate;
        d2 += ThreadLocalRandom.current().nextGaussian() * accurate;
        double d3 = (double) MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
        retr[0] = d0 / d3 * 0.1D;
        retr[1] = d1 / d3 * 0.1D;
        retr[2] = d2 / d3 * 0.1D;
        return retr;
    }
}
