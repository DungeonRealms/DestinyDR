package net.dungeonrealms.game.world.entity.type.pet;

import net.minecraft.server.v1_9_R2.EntityPigZombie;
import net.minecraft.server.v1_9_R2.World;

/**
 * ZombigPigPet - A zombie pig pet.
 * 
 * Redone on April 22nd, 2017
 * @author Kneesnap
 */
public class ZombiePigPet extends EntityPigZombie {

    public ZombiePigPet(World world) {
        super(world);
        setBaby(true);
        this.angerLevel = 0;
    }

    @Override
    protected void r() {
    	// Prevents registering default AI goals.
    }
}
