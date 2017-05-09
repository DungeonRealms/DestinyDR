package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

/**
 * Created by Xwaffle on 8/29/2015.
 */
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
