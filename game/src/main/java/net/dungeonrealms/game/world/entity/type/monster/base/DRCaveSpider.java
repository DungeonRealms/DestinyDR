package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public abstract class DRCaveSpider extends EntitySpider implements DRMonster {

    public DRCaveSpider(World world) {
        super(world);
        
        //  SET NMS DATA  //
        goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    @Override
    public EnumMonster getEnum() {
        return EnumMonster.Spider2;
    }
    
    @Override
    public void collide(Entity e) {}

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }

}
