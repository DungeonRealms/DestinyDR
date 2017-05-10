package net.dungeonrealms.game.world.entity.type.pet;

import net.minecraft.server.v1_9_R2.EntityOcelot;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;

/**
 * Ocelot pet.
 * 
 * Redone on April 22nd, 2017.
 * @author Kneesnap
 */
public class OcelotPet extends EntityOcelot {

    public OcelotPet(World world) {
        super(world);
        setAgeRaw(-24000);
        this.ageLocked = true;
        setSitting(false);
        setTamed(true);
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.45F);
    }

    @Override
    protected void r() {
    	// Prevents registering default AI goals.
    }
}
