package net.dungeonrealms.game.world.entity.type.monster.type.melee;

import net.dungeonrealms.game.world.entity.type.monster.base.DRZombie;
import net.dungeonrealms.game.world.entity.util.pathfinders.PathfinderGoalMeleeAttackWell;
import net.minecraft.server.v1_9_R2.World;

public class MeleeZombie extends DRZombie {
    public MeleeZombie(World world) {
    	super(world);
    }

    @Override
    protected void r() {
        super.r();
        this.goalSelector.a(2, new PathfinderGoalMeleeAttackWell(this, 1.0D, false));
    }
}
