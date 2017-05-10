package net.dungeonrealms.game.world.entity.type.pet;

import net.minecraft.server.v1_9_R2.EntityCreeper;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;

/**
 * CreeperPet - A creeper pet.
 * 
 * Redone on April 22nd, 2017.
 * @author Kneesnap
 */
public class CreeperPet extends EntityCreeper {

    public CreeperPet(World world) {
        super(world);
        setPowered(true);
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.45F);
    }

    @Override
    protected void r() {
    	//Prevents registering default AI goals.
    }
}
