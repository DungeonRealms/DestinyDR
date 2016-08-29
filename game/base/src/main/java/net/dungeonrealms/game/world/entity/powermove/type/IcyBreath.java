package net.dungeonrealms.game.world.entity.powermove.type;

import net.dungeonrealms.game.world.entity.powermove.PowerMove;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/23/2016
 */

public class IcyBreath extends PowerMove {

    public IcyBreath() {
        super("icybreath");
    }

    @Override
    public void schedulePowerMove(LivingEntity entity, Player attack) {
        Vector vector = entity.getEyeLocation().getDirection();
        vector.multiply(2.0F);

        Random random = new Random();

        float angle = entity.getEyeLocation().getYaw() / 60;

        for (int i = 0; i < random.nextInt(6) + 1; i++) {
            FallingBlock ice = entity.getWorld().spawnFallingBlock(entity.getEyeLocation().add(Math.cos(angle) * -0.50, -1.07, Math.sin(angle) * -0.50), Material.FROSTED_ICE, (byte) 0);
            ice.setVelocity(vector);
        }
    }
}
