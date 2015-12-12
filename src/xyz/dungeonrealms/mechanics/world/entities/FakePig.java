package xyz.dungeonrealms.mechanics.world.entities;

import net.minecraft.server.v1_8_R3.*;
import xyz.dungeonrealms.mechanics.world.Tier;
import xyz.dungeonrealms.mechanics.world.interfaces.DRMob;
import xyz.dungeonrealms.mechanics.world.interfaces.Playable;

/**
 * Created by Nick on 12/11/2015.
 */
public class FakePig extends EntityPig implements Playable, DRMob {

    public FakePig(World world) {
        super(world);
        ((Navigation) this.getNavigation()).a(true);
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new PathfinderGoalPanic(this, 1.25D));
        this.goalSelector.a(3, new PathfinderGoalBreed(this, 1.0D));
        this.goalSelector.a(4, new PathfinderGoalTempt(this, 1.2D, Items.CARROT_ON_A_STICK, false));
        this.goalSelector.a(4, new PathfinderGoalTempt(this, 1.2D, Items.CARROT, false));
        this.goalSelector.a(5, new PathfinderGoalFollowParent(this, 1.1D));
        this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
    }

    @Override
    protected String z() {
        return "mob.pig.say";
    }

    @Override
    protected String bo() {
        return "mob.pig.say";
    }

    @Override
    protected String bp() {
        return "mob.pig.death";
    }

    @Override
    public String getIdentifier() {
        return "FakePig";
    }

    @Override
    public Tier getMobTier() {
        return Tier.TIER_1;
    }
}
