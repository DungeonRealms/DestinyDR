package net.dungeonrealms.game.world.entity.type.monster.base;

import lombok.Getter;
import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 09-Jun-16.
 */
public class DRGhast extends EntityGhast implements DRMonster {

    EnumMonster monster;
    @Getter
    protected AttributeList attributes = new AttributeList();

    public DRGhast(World world) {
        super(world);
    }

    public DRGhast(World world, EnumMonster mon, int tier) {
        this(world);
        monster = mon;
        setupMonster(tier);
    }

    @Override
    public ItemStack getWeapon() {
    	return makeItem(new ItemWeaponStaff());
    }

    @Override
    public void collide(Entity e) {}

    @Override
    public EnumMonster getEnum() {
        return monster;
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
