package net.dungeonrealms.game.world.entity.type.monster.type.melee;

import net.dungeonrealms.game.world.entity.type.monster.base.DRSpider;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class LargeSpider extends DRSpider {
    public LargeSpider(World world) {
        this(world, 1);
    }
    
    public LargeSpider(World world, int tier) {
        super(world, tier);
    }
}
