package net.dungeonrealms.game.world.entity.type.monster.type.melee;

import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRWitherSkeleton;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class MeleeWitherSkeleton extends DRWitherSkeleton {
    public MeleeWitherSkeleton(World world) {
        this(world, 1, EnumMonster.Skeleton1);
    }

    public MeleeWitherSkeleton(World world, int tier, EnumMonster monster) {
        super(world, monster, tier);
        this.setSize(0.7F, 2.4F);
        this.fireProof = true;
        this.setSkeletonType(1);
    }
}
