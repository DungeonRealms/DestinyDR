package net.dungeonrealms.game.world.entity.type.pet;

import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.EntitySlime;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;

/**
 * SlimePets
 * 
 * Redone on April 22nd, 2017.
 * @author Kneesnap
 */
public class SlimePet extends EntitySlime {

	public SlimePet(World world) {
        super(world);
        setSize(1);
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.45F);
    }
	
    @Override
    protected void d(EntityLiving entityliving) {
    	// Prevents vanilla attacks.
    }

    @Override
    protected void r() {
    	// Prevents registering default AI.
    }
}
