package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.pathfinders.PathfinderGoalMeleeAttackWell;
import net.minecraft.server.v1_9_R2.*;

public abstract class DRZombie extends EntityZombie implements DRMonster {

    protected EnumMonster monsterType;
    
    protected DRZombie(World w) {
    	super(w);
    }
    
    protected DRZombie(World world, EnumMonster m) {
    	super(world);
    	setMonster(m);
    }

    @Override
    protected void r() {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.o();
    }

    @Override
    protected void o() {
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true, new Class[]{EntityPigZombie.class, EntitySkeleton.class, EntityZombie.class}));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    @Override
    public void collide(Entity e) {}
    
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
