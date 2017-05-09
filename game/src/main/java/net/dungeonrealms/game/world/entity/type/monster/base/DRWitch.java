package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 09-Jun-16.
 */
public class DRWitch extends EntityWitch implements DRMonster {
    
    public DRWitch(World world) {
        super(world);
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(2, new PathfinderGoalArrowAttack(this, 1.0D, 5, 30, 10.0F));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    @Override
    public ItemStack getWeapon() {
        return makeItem(new ItemWeaponStaff());
    }

    @Override
    public void collide(Entity e) {
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if(damagesource == DamageSource.MAGIC)
            return false;
        return super.damageEntity(damagesource, f);
    }

    @Override
    public EnumMonster getEnum() {
        return EnumMonster.Witch;
    }

    public void a(EntityLiving var1, float var2) {
        if (!this.o()) {
            double var3 = var1.locY + (double) var1.getHeadHeight() - 1.100000023841858D;
            double var5 = var1.locX + var1.motX - this.locX;
            double var7 = var3 - this.locY;
            double var9 = var1.locZ + var1.motZ - this.locZ;
            float var11 = MathHelper.sqrt(var5 * var5 + var9 * var9);
            PotionRegistry var12 = Potions.x;
            if (var11 >= 8.0F && !var1.hasEffect(MobEffects.SLOWER_MOVEMENT)) {
                var12 = Potions.r;
            } else if (var1.getHealth() >= 8.0F && !var1.hasEffect(MobEffects.POISON)) {
                var12 = Potions.z;
            } else if (var11 <= 3.0F && !var1.hasEffect(MobEffects.WEAKNESS) && this.random.nextFloat() < 0.25F) {
                var12 = Potions.I;
            }

            EntityPotion var13 = new EntityPotion(this.world, this, PotionUtil.a(new net.minecraft.server.v1_9_R2.ItemStack(Items.SPLASH_POTION), var12));
            var13.pitch -= -20.0F;
            var13.shoot(var5, var7 + (double) (var11 * 0.2F), var9, 0.75F, 8.0F);
            this.world.a((EntityHuman) null, this.locX, this.locY, this.locZ, SoundEffects.gE, this.bA(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
            this.world.addEntity(var13);
            //Make sure its registering the data.
            MetadataUtils.registerProjectileMetadata(getAttributes(), getTier(), (Projectile)var13.getBukkitEntity());
        }
    }


    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }
}
