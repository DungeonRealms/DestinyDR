package net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff;

import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRSkeleton;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.Item.GeneratedItemType;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class StaffSkeleton extends DRSkeleton implements IRangedEntity {

    public StaffSkeleton(World world, EnumMonster mons, int tier) {
        super(world, mons, tier);
        
        //  SET NMS DATA  //
        clearGoalSelectors();
        this.goalSelector.a(0, new PathfinderGoalRandomStroll(this, .6F));
        this.goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(2, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(4, new PathfinderGoalArrowAttack(this, 1.0D, 15, 60, 15.0F));
    }

    @Override
    public org.bukkit.inventory.ItemStack getWeapon() {
        return makeItem(new ItemWeaponStaff());
    }

    @Override
    public void a(EntityLiving entity, float f) {
    	DamageAPI.fireStaffProjectile((LivingEntity)getBukkitEntity(), new ItemWeaponStaff(getHeld()));
    }

    private void clearGoalSelectors() {
        try {
            Field a = PathfinderGoalSelector.class.getDeclaredField("b");
            Field b = PathfinderGoalSelector.class.getDeclaredField("c");
            a.setAccessible(true);
            b.setAccessible(true);
            ((LinkedHashSet) a.get(this.goalSelector)).clear();
            ((LinkedHashSet) b.get(this.goalSelector)).clear();
        } catch(Throwable e) {
            e.printStackTrace();
        }
    }
}
