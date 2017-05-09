package net.dungeonrealms.game.world.entity.type.monster.type.melee;

import net.dungeonrealms.game.world.entity.type.monster.base.DRCaveSpider;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.World;
/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class SmallSpider extends DRCaveSpider {

    public SmallSpider(World world) {
        super(world);
    }
    
    @Override
    public EnumMonster getEnum() {
    	return EnumMonster.Spider2;
    }
}
