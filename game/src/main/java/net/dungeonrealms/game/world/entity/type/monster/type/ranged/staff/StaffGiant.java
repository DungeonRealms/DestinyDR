package net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff;

import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.world.entity.type.monster.base.DRGiant;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

/**
 * Created by Rar349 on 6/8/2017.
 */
public class StaffGiant extends DRGiant implements IRangedEntity{
    public StaffGiant(World world) {
        super(world);
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(25D);
        EntityAPI.clearAI(goalSelector, targetSelector);

        this.goalSelector.a(0, new PathfinderGoalArrowAttack(this, 1.3D, 30, 45, 15.0F));
        this.goalSelector.a(1, new PathfinderGoalRandomStroll(this, .7F));
        this.goalSelector.a(2, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(3, new PathfinderGoalRandomLookaround(this));

        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false, untargettable));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    @Override
    public org.bukkit.inventory.ItemStack getWeapon() {
        return makeItem(new ItemWeaponStaff());
    }

    @Override
    protected void r() {}


    @Override
    public void collide(Entity e) {
        if (e != null && e instanceof Projectile)
            if (!(((Projectile)e).getShooter() instanceof Player))
                return;
        super.collide(e);
    }

    @Override
    public void a(EntityLiving entityLiving, float v) {
        DamageAPI.fireStaffProjectile((LivingEntity)getBukkitEntity(), new ItemWeaponStaff(getHeld()));
        System.out.println("Fired his staff projectile!");
    }
}
