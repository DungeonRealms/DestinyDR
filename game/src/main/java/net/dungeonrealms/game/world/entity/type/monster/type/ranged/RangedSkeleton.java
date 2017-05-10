package net.dungeonrealms.game.world.entity.type.monster.type.ranged;

import net.dungeonrealms.game.item.items.core.ItemWeaponBow;
import net.dungeonrealms.game.world.entity.type.monster.base.DRSkeleton;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.IRangedEntity;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class RangedSkeleton extends DRSkeleton implements IRangedEntity {

    public RangedSkeleton(World world) {
        super(world);
    }

    @Override
    public ItemStack getWeapon() {
    	return makeItem(new ItemWeaponBow());
    }

    @Override
    public void a(EntityLiving entityliving, float f) {
        DamageAPI.fireBowProjectile((LivingEntity) this.getBukkitEntity(), new ItemWeaponBow(getHeld()));
    }
}
