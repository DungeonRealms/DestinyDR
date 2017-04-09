package net.dungeonrealms.game.world.entity.type.monster.base;

import lombok.Getter;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

/**
 * Created by Chase on Oct 18, 2015
 */
public class DRPigman extends EntityPigZombie implements DRMonster {

	@Getter
	protected AttributeList attributes = new AttributeList();
	
	public DRPigman(World world) {
		super(world);
	}
	
	public DRPigman(World world, int tier) {
		this(world);
		setupMonster(tier);
		
		//  SET NMS DATA  //
		this.angerLevel = 30000;
		this.targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
	}

	@Override
	public void collide(Entity e) {}

	@Override
	public EnumMonster getEnum() {
		return EnumMonster.Daemon;
	}

	@Override
	public void enderTeleportTo(double d0, double d1, double d2) {
		//Test for EnderPearl TP Cancel.
	}

	@Override
	public EntityLiving getNMS() {
		return this;
	}
}
