package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

/**
 * Created by Kieran Quigley (Proxying) on 06-Jun-16.
 */
public class DRWolf extends EntityWolf implements DRMonster {
    
    public DRWolf(World world) {
    	super(world);
    	
    	//  SET NMS DATA  //
        a(0.6F, 0.8F);
        this.getAttributeInstance(GenericAttributes.maxHealth).setValue(20);
        setHealth(getMaxHealth());
        this.setAngry(true);
        this.setTamed(false);
        this.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    @Override
    public void collide(Entity e) {}

    @Override
    public EnumMonster getEnum() {
        return EnumMonster.Wolf;
    }

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }
}
