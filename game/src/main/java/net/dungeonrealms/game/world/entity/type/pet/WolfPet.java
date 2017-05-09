package net.dungeonrealms.game.world.entity.type.pet;

import net.minecraft.server.v1_9_R2.EntityWolf;
import net.minecraft.server.v1_9_R2.World;

/**
 * WolfPets
 * 
 * Redone on April 22nd, 2017.
 * @author Kneesnap
 */
public class WolfPet extends EntityWolf {

    public WolfPet(World world) {
        super(world);
        setSitting(false);
        setAngry(false);
        setTamed(true);
        this.ageLocked = true;
        setAge(0);
        setHealth(getMaxHealth()); //TODO: ???
    }

    @Override
    protected void r() {
    	// Prevents registering default AI goals.
    }
}
