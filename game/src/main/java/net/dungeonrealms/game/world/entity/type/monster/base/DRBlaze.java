package net.dungeonrealms.game.world.entity.type.monster.base;

import lombok.Getter;
import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.inventory.ItemStack;

/**
 * Created by Chase on Oct 4, 2015
 */
public abstract class DRBlaze extends EntityBlaze implements DRMonster {
	
	@Getter
	protected AttributeList attributes = new AttributeList();
	
	protected DRBlaze(World world) {
		super(world);
	}

	public DRBlaze(World world, int tier) {
		this(world);
		setupMonster(tier);
		
		//  SET NMS DATA //
		goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
	}
	
	@Override
	public EnumMonster getEnum() {
		return EnumMonster.Blaze;
	}
	
	@Override
	public EntityLiving getNMS() {
		return this;
	}
	
	@Override
	public ItemStack getWeapon() {
		return makeItem(new ItemWeaponStaff());
	}

	@Override
	public void collide(Entity e) {
	}

	@Override
	public void enderTeleportTo(double d0, double d1, double d2) {
		//Test for EnderPearl TP Cancel.
	}
}
