package net.dungeonrealms.game.world.entity.type.monster.type.ranged;

import net.dungeonrealms.game.item.items.core.ItemWeaponBow;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRZombie;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.pathfinders.PathFinderShootBow;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class RangedZombie extends DRZombie implements IRangedEntity {

    public RangedZombie(World world) {
        super(world, EnumMonster.Bandit);
        
        EntityAPI.clearAI(goalSelector, targetSelector);
        this.goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(1, new PathFinderShootBow(this, 1.0D, 20, 15.0F));
        this.goalSelector.a(1, new PathfinderGoalRandomStroll(this, .6F));
        this.goalSelector.a(2, new PathfinderGoalRandomLookaround(this));


        this.targetSelector.a(0, new PathfinderGoalHurtByTarget(this, false, untargettable));
        this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    @Override
    public ItemStack getWeapon() {
    	return makeItem(new ItemWeaponBow());
    }

    @Override
    public void a(EntityLiving entityLiving, float v) {
        DamageAPI.fireBowProjectile((LivingEntity)getBukkitEntity(), new ItemWeaponBow(getHeld()));
    }
}
