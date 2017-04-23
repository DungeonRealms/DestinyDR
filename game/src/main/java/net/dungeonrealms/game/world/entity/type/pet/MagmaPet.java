package net.dungeonrealms.game.world.entity.type.pet;

import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.EntityMagmaCube;
import net.minecraft.server.v1_9_R2.World;

/**
 * MagmaPets
 * 
 * Redone on April 22nd, 2017.
 * @author Kneesnap
 */
public class MagmaPet extends EntityMagmaCube {

    public MagmaPet(World world) {
        super(world);
        setSize(1);
    }

    @Override
    protected void d(EntityLiving entityliving) {
    	// Prevents vanilla attacks.
    }

    @Override
    protected void r() {
    	// Prevents registering default AI goals.
    }
}
