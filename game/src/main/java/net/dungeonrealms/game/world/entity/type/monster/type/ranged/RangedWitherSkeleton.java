package net.dungeonrealms.game.world.entity.type.monster.type.ranged;

import net.dungeonrealms.game.item.items.core.ItemWeaponBow;
import net.dungeonrealms.game.world.entity.type.monster.base.DRWitherSkeleton;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class RangedWitherSkeleton extends DRWitherSkeleton implements IRangedEntity {

    public RangedWitherSkeleton(World world) {
        super(world, null);
    }
    
    public ItemStack getWeapon() {
    	return makeItem(new ItemWeaponBow());
    }

    @Override
    public void a(EntityLiving entityliving, float f) {
    	DamageAPI.fireBowProjectile((LivingEntity)getBukkitEntity(), new ItemWeaponBow(getHeld()));
    }
}
