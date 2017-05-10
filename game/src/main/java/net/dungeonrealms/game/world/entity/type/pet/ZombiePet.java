package net.dungeonrealms.game.world.entity.type.pet;

import net.minecraft.server.v1_9_R2.EntityZombie;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;

/**
 * ZombiePet - A zombie pet.
 * 
 * Redone on April 22nd, 2017.
 * @author Kneesnap
 */
public class ZombiePet extends EntityZombie {

    public ZombiePet(World world) {
        super(world);
        setBaby(true);
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.45F);
    }

    @Override
    protected void r() {
    	// Prevents registering default AI goals.
    }
}
