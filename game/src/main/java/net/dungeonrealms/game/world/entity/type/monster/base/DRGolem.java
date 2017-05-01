package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public abstract class DRGolem extends EntityIronGolem implements DRMonster {
    
    protected DRGolem(World world) {
        super(world);
        
        //  SET NMS DATA  //
        this.setPlayerCreated(false);
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }
    
    @Override
    public EnumMonster getEnum(){
    	return EnumMonster.Golem;
    }

    @Override
    public void collide(Entity e) {}

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }
}
