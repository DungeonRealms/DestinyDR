package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

/**
 * Created by Chase on Oct 2, 2015
 */
public abstract class DRSpider extends EntitySpider implements DRMonster {

	public DRSpider(World world) {
		super(world);
		this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
		this.targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
	}
	@Override
	public EnumMonster getEnum() {
		return EnumMonster.Spider1;
	}

	@Override
	public void collide(Entity e) {}

	@Override
	public void enderTeleportTo(double d0, double d1, double d2) {
		//Test for EnderPearl TP Cancel.
	}
}
