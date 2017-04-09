package net.dungeonrealms.game.world.entity.type.monster.base;

import lombok.Getter;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.inventory.ItemStack;

/**
 * Created by Chase on Oct 21, 2015
 */
public class DRSilverfish extends EntitySilverfish implements DRMonster {

	public EnumMonster enumMonster;
	@Getter
	protected AttributeList attributes = new AttributeList();

	public DRSilverfish(World world) {
		super(world);
	}
	
	public DRSilverfish(World world, EnumMonster type, int tier) {
		this(world);
		this.enumMonster = type;
		setupMonster(tier);
		
		this.targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
	}

	@Override
	public ItemStack getWeapon() {
		return makeItem(new ItemWeapon());
	}

	@Override
	public void collide(Entity e) {}

	@Override
	public EnumMonster getEnum() {
		return enumMonster;
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
