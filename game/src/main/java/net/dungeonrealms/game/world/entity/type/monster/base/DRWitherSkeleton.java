package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityCombustByEntityEvent;

/**
 * Created by Chase on Oct 3, 2015
 */
public abstract class DRWitherSkeleton extends DRSkeleton implements DRMonster {

	protected DRWitherSkeleton(World world, EnumMonster m) {
		super(world, m);
        setSkeletonType(1);
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        this.setSize(0.7F, 2.4F);
        this.fireProof = true;
	}

    @Override
    protected Item getLoot() {
        return null;
    }

    @Override
    public boolean B(Entity entity) {
    	return damage(entity);
    	//Should prevent wither effect being added on.
    }

    public boolean damage(Entity entity) {
        float f = (float)this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue();
        int i = 0;
        if(entity instanceof EntityLiving) {
            f += EnchantmentManager.a(this.getItemInMainHand(), ((EntityLiving)entity).getMonsterType());
            i += EnchantmentManager.a(this);
        }

        boolean flag = entity.damageEntity(DamageSource.mobAttack(this), f);
        if(flag) {
            if(i > 0 && entity instanceof EntityLiving) {
                ((EntityLiving)entity).a(this, (float)i * 0.5F, (double)MathHelper.sin(this.yaw * 0.017453292F), (double)(-MathHelper.cos(this.yaw * 0.017453292F)));
                this.motX *= 0.6D;
                this.motZ *= 0.6D;
            }

            int j = EnchantmentManager.getFireAspectEnchantmentLevel(this);
            if(j > 0) {
                EntityCombustByEntityEvent entityhuman = new EntityCombustByEntityEvent(this.getBukkitEntity(), entity.getBukkitEntity(), j * 4);
                Bukkit.getPluginManager().callEvent(entityhuman);
                if(!entityhuman.isCancelled()) {
                    entity.setOnFire(entityhuman.getDuration());
                }
            }

            if(entity instanceof EntityHuman) {
                EntityHuman entityhuman1 = (EntityHuman)entity;
                net.minecraft.server.v1_9_R2.ItemStack itemstack = this.getItemInMainHand();
                net.minecraft.server.v1_9_R2.ItemStack itemstack1 = entityhuman1.ct()?entityhuman1.cw():null;
                if(itemstack != null && itemstack1 != null && itemstack.getItem() instanceof ItemAxe && itemstack1.getItem() == Items.SHIELD) {
                    float f1 = 0.25F + (float)EnchantmentManager.getDigSpeedEnchantmentLevel(this) * 0.05F;
                    if(this.random.nextFloat() < f1) {
                        entityhuman1.db().a(Items.SHIELD, 100);
                    }
                }
            }

            this.a(this, entity);
        }

        return flag;
    }
    
    @Override
	public void a(EntityLiving entityliving, float f) {}
}
