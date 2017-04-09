package net.dungeonrealms.game.world.entity.type.monster.type.ranged;

import net.dungeonrealms.game.item.items.core.ItemWeaponBow;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRSkeleton;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class RangedSkeleton extends DRSkeleton implements IRangedEntity {

    public RangedSkeleton(World world, EnumMonster monsterType, int tier) {
        super(world, monsterType, tier);
    }

    @Override
    public ItemStack getWeapon() {
    	return makeItem(new ItemWeaponBow());
    }

	@Override
	public void a(EntityLiving entityliving, float f) {
		
	}
}
