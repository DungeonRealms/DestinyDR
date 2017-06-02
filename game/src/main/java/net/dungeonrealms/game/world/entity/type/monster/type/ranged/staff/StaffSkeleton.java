package net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff;

import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.world.entity.type.monster.base.DRSkeleton;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;

public class StaffSkeleton extends DRSkeleton implements IRangedEntity {

    private static Field bowPathFinder = null, meleePathFinder = null;

    public StaffSkeleton(World world) {
        super(world);
    }

    @Override
    public org.bukkit.inventory.ItemStack getWeapon() {
        return makeItem(new ItemWeaponStaff());
    }

    @Override
    public void a(EntityLiving entity, float f) {
        System.out.println("Calling staff skeleton shoot!");
        DamageAPI.fireStaffProjectile((LivingEntity) getBukkitEntity(), new ItemWeaponStaff(getHeld()));
    }

    @Override
    public void o() {
    }

    @Override
    protected void r() {
        super.r();
        this.goalSelector.a(4, new PathfinderGoalArrowAttack(this, 1.0D, 25, 40, 25.0F));
    }

    //    @Override
//    protected void r() {
//        this.targetSelector.a(0, new PathfinderGoalHurtByTarget(this, false, EntityHuman.class));
//        this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
//    }
}
