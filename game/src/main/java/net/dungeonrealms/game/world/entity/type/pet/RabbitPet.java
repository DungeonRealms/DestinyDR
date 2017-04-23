package net.dungeonrealms.game.world.entity.type.pet;

import net.minecraft.server.v1_9_R2.EntityRabbit;
import net.minecraft.server.v1_9_R2.World;

/**
 * RabbitPets
 * 
 * Redone on April 22nd, 2017.
 * @author Kneesnap
 */
public class RabbitPet extends EntityRabbit {

    public RabbitPet(World world) {
        super(world);
        setAge(-1);
        this.ageLocked = true;
    }
}
