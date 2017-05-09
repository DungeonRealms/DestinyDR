package net.dungeonrealms.game.world.entity.type.pet;

import net.minecraft.server.v1_9_R2.EntitySilverfish;
import net.minecraft.server.v1_9_R2.World;

/**
 * SiverfishPet
 * 
 * Redone April 22nd, 2017.
 * @author Kneesnap
 */
public class SilverfishPet extends EntitySilverfish {

    public SilverfishPet(World world) {
        super(world);
    }

    @Override
    protected void r() {
    	// Prevents registering default AI goals.
    }
}
