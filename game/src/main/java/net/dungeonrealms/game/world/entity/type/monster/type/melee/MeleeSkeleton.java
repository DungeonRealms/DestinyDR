package net.dungeonrealms.game.world.entity.type.monster.type.melee;

import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRSkeleton;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.World;

public class MeleeSkeleton extends DRSkeleton {

	public MeleeSkeleton(World world) {
		super(world, EnumMonster.Skeleton);
	}

    @Override
    public void a(EntityLiving entityliving, float f) {}
}
