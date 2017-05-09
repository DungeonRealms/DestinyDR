package net.dungeonrealms.game.world.entity.type.pet;


import net.minecraft.server.v1_9_R2.EntityCaveSpider;
import net.minecraft.server.v1_9_R2.World;

/**
 * SpiderPet - A pet spider.
 * 
 * Redone on April 22nd, 2017.
 * @author Kneesnap
 */
public class SpiderPet extends EntityCaveSpider {

    public SpiderPet(World world) {
        super(world);
    }
    
    @Override
    protected void r() {
    	// Prevents registering default AI goals.
    }
}
