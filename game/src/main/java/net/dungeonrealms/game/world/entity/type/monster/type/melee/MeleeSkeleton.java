package net.dungeonrealms.game.world.entity.type.monster.type.melee;

import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRSkeleton;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Kieran Quigley (Proxying) on 15-Jun-16.
 */
public class MeleeSkeleton extends DRSkeleton {

	public MeleeSkeleton(World world) {
		this(world, EnumMonster.Skeleton, 1);
	}
	
    public MeleeSkeleton(World world, EnumMonster monsterType, int tier) {
        super(world, monsterType, tier);
    }

    @Override
    public void a(EntityLiving entityliving, float f) {
    	
    }
}
