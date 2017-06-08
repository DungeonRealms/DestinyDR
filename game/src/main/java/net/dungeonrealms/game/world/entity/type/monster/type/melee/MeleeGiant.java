package net.dungeonrealms.game.world.entity.type.monster.type.melee;

import net.dungeonrealms.game.world.entity.type.monster.base.DRGiant;
import net.dungeonrealms.game.world.entity.util.pathfinders.PathfinderGoalMeleeAttackWell;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Rar349 on 6/8/2017.
 */
public class MeleeGiant extends DRGiant {

    public MeleeGiant(World world) {
        super(world);
    }

    @Override
    protected void r() {
        super.r();
        this.goalSelector.a(2, new PathfinderGoalMeleeAttackWell(this, 1.0D, false));
    }
}
