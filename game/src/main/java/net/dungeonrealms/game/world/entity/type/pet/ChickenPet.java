package net.dungeonrealms.game.world.entity.type.pet;

import net.minecraft.server.v1_9_R2.EntityChicken;
import net.minecraft.server.v1_9_R2.World;

/**
 * ChickenPet - A pet chicken.
 * 
 * Redone on April 22nd, 2017.
 * @author Kneesnap
 */
public class ChickenPet extends EntityChicken {

    public ChickenPet(World world) {
        super(world);
        setAge(-1);
        this.ageLocked = true;
    }
}
