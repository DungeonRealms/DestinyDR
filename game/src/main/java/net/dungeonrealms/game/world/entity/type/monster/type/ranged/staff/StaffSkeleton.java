package net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff;

import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.world.entity.type.monster.base.DRSkeleton;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.entity.LivingEntity;
import sun.reflect.Reflection;

import java.lang.reflect.Field;

public class StaffSkeleton extends DRSkeleton implements IRangedEntity {

    public static Field bowPathFinder = null;

    public StaffSkeleton(World world) {
        super(world);

        /*
        EntityAPI.clearAI(goalSelector, targetSelector);
        this.goalSelector.a(0, new PathfinderGoalRandomStroll(this, .6F));
        this.goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(2, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(4, new PathfinderGoalArrowAttack(this, 1.0D, 15, 60, 15.0F));*/
    }

    @Override
    public org.bukkit.inventory.ItemStack getWeapon() {
        return makeItem(new ItemWeaponStaff());
    }

    @Override
    public void a(EntityLiving entity, float f) {
        System.out.println("Calling staff skeleton shoot!");
    	DamageAPI.fireStaffProjectile((LivingEntity)getBukkitEntity(), new ItemWeaponStaff(getHeld()));
    }

    @Override
    public void o() {
        super.o();
        if(bowPathFinder == null) {
            try {
                bowPathFinder = EntitySkeleton.class.getDeclaredField("c");
                bowPathFinder.setAccessible(true);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        try {

            byte b0 = 20;
            if (this.world.getDifficulty() != EnumDifficulty.HARD) {
                b0 = 40;
            }

            PathfinderGoalBowShoot goalBow = (PathfinderGoalBowShoot) bowPathFinder.get(this);
            goalBow.b(b0);
            this.goalSelector.a(4, goalBow);
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}
