package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.inventory.ItemStack;

/**
 * Created by Chase on Oct 17, 2015
 */
public class DRMagma extends EntityMagmaCube implements DRMonster {
	
	public DRMagma(World world) {
		super(world);
		
		//  SET NMS DATA  //
		setSize(4);
		this.setSize(0.51000005F * (float)4, 0.51000005F * (float)4);
		this.setPosition(this.locX, this.locY, this.locZ);
		this.getAttributeInstance(GenericAttributes.maxHealth).setValue((double)(4 * 4));
		this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue((double)(0.2F + 0.1F * (float)4));
		this.setHealth(this.getMaxHealth());
		this.fireProof = true;
		this.b_ = 4;
	}
	
	@Override
    public ItemStack getWeapon() {
        return makeItem(new ItemWeapon());
    }

	@Override
	public void collide(Entity e) {}
	
	@Override
	public EnumMonster getEnum() {
		return EnumMonster.MagmaCube;
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
