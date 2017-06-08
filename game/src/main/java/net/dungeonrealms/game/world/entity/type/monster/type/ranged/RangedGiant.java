package net.dungeonrealms.game.world.entity.type.monster.type.ranged;

import net.dungeonrealms.game.item.items.core.ItemWeaponBow;
import net.dungeonrealms.game.world.entity.type.monster.base.DRGiant;
import net.dungeonrealms.game.world.entity.util.pathfinders.PathFinderShootBow;
import net.dungeonrealms.game.world.entity.util.pathfinders.PathFinderShootBowGiant;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.IRangedEntity;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 6/8/2017.
 */
public class RangedGiant extends DRGiant implements IRangedEntity {

    public RangedGiant(World world) {
        super(world);
    }

    @Override
    protected void r() {
        super.r();
        this.goalSelector.a(1, new PathFinderShootBowGiant(this, 1.0D, 20, 15.0F));
    }

    @Override
    public ItemStack getWeapon() {
        return makeItem(new ItemWeaponBow());
    }

    @Override
    public void a(EntityLiving entityLiving, float v) {
        DamageAPI.fireBowProjectile((LivingEntity) getBukkitEntity(), new ItemWeaponBow(getHeld()));
    }
}
