package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

public abstract class DRSkeleton extends EntitySkeleton implements DRMonster {
    protected EnumMonster monsterType;
    
    protected DRSkeleton(World world, EnumMonster type) {
    	this(world);
    	setMonster(type);
    }

    public DRSkeleton(World world) {
    	super(world);
    }

    @Override //This is the shoot arrow method I believe.
    public abstract void a(EntityLiving entityliving, float f);

    @Override
    public void collide(Entity e) {}

    @Override
    protected void r() {
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(3, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(4, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new PathfinderGoalHurtByTarget(this, false, EntityHuman.class));
        this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    @Override
    public void setMonster(EnumMonster m) {
    	this.monsterType = m;
    }
    
	@Override
	public EnumMonster getEnum(){
		return this.monsterType;
	}

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }
}
