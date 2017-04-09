package net.dungeonrealms.game.world.entity.type.monster.base;

import lombok.Getter;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

/**
 * Created by Xwaffle on 8/29/2015.
 */

public abstract class DRZombie extends EntityZombie implements DRMonster {

    protected EnumMonster monsterType;
    @Getter
    protected AttributeList attributes = new AttributeList();
    
    public DRZombie(World world) {
    	super(world);
    }
    
    protected DRZombie(World world, EnumMonster monster, int tier) {
        this(world);
        this.monsterType = monster;
        setupMonster(tier);
    }

    @Override
    public void collide(Entity e) {}
	
	@Override
	public EnumMonster getEnum(){
		return this.monsterType;
	}

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }
    
    public EntityLiving getNMS() {
    	return this;
    }
}
