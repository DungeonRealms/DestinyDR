package net.dungeonrealms.game.world.entity.type.monster.base;

import lombok.Getter;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

/**
 * Created by Chase on Sep 19, 2015
 */
public abstract class DRSkeleton extends EntitySkeleton implements DRMonster {
    protected EnumMonster monsterType;
    @Getter
    protected AttributeList attributes = new AttributeList();

    public DRSkeleton(World world) {
    	super(world);
    }
    
    protected DRSkeleton(World world, EnumMonster monster, int tier) {
        this(world);
        monsterType = monster;
        setupMonster(tier);
    }

    @Override //This is the shoot arrow method I believe.
    public abstract void a(EntityLiving entityliving, float f);

    @Override
    public void collide(Entity e) {}
    
	@Override
	public EnumMonster getEnum(){
		return this.monsterType;
	};

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }
    
    public EntityLiving getNMS() {
    	return this;
    }
}
