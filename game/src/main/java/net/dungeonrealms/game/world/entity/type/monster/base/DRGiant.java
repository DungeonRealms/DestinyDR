package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.entity.Giant;

/**
 * Created by Rar349 on 6/7/2017.
 */
public class DRGiant extends EntityGiantZombie implements DRMonster {

    public DRGiant(World world) {
        super(world);
    }


    @Override
    public EnumMonster getEnum() {
        return EnumMonster.Giant;
    }

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }

    @Override
    protected void r() {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));

        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true, new Class[]{EntityPigZombie.class, EntitySkeleton.class, EntityZombie.class}));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }
}
