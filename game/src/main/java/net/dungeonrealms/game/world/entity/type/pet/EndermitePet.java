package net.dungeonrealms.game.world.entity.type.pet;

import net.minecraft.server.v1_9_R2.EntityEndermite;
import net.minecraft.server.v1_9_R2.World;

/**
 * Endermite Pets
 * 
 * Redone on April 2nd, 2017.
 * @author Kneesnap
 */
public class EndermitePet extends EntityEndermite {

    public EndermitePet(World world) {
        super(world);
    }

    @Override
    protected void r() {
    	// Prevents registering default AI goals.
    }
}
