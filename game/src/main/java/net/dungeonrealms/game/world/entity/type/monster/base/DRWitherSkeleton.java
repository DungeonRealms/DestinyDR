package net.dungeonrealms.game.world.entity.type.monster.base;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chase on Oct 3, 2015
 */
public abstract class DRWitherSkeleton extends EntitySkeleton implements DRMonster {

    public EnumMonster enumMonster;
    @Getter
    protected Map<String, Integer[]> attributes = new HashMap<>();

    public DRWitherSkeleton(World world) {
        super(world);
    }

    public DRWitherSkeleton(World world, EnumMonster mon, int tier, EnumEntityType entityType) {
        super(world);
        enumMonster = mon;
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(20d);
        this.getAttributeInstance(GenericAttributes.c).setValue(1.00d);
        setSkeletonType(1);
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        if (livingEntity instanceof Skeleton) {
            ((Skeleton) livingEntity).setSkeletonType(Skeleton.SkeletonType.WITHER);
        }
        setArmor(tier);
        setStats();
        String customName = enumMonster.getPrefix() + " " + enumMonster.name + " " + enumMonster.getSuffix() + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        this.setSize(0.7F, 2.4F);
        this.fireProof = true;
        this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(enumMonster.getSkullItem(enumMonster)));
        livingEntity.getEquipment().setHelmet(enumMonster.getSkullItem(enumMonster));
        this.noDamageTicks = 0;
        this.maxNoDamageTicks = 0;
    }

    protected abstract void setStats();

    @Override
    protected Item getLoot() {
        return null;
    }

    public void setArmor(int tier) {
        ItemStack[] armor = GameAPI.getTierArmor(tier);
        // weapon, boots, legs, chest, helmet/head
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        boolean armorMissing = false;
        int chance = 6 + tier;
        if (tier >= 3 || random.nextInt(10) <= chance) {
            ItemStack armor0 = AntiDuplication.getInstance().applyAntiDupe(armor[0]);
            livingEntity.getEquipment().setBoots(armor0);
            this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor0));
        } else {
            armorMissing = true;
        }
        if (tier >= 3 || random.nextInt(10) <= chance || armorMissing) {
            ItemStack armor1 = AntiDuplication.getInstance().applyAntiDupe(armor[1]);
            livingEntity.getEquipment().setLeggings(armor1);
            this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor1));
            armorMissing = false;
        } else {
            armorMissing = true;
        }
        if (tier >= 3 || random.nextInt(10) <= chance || armorMissing) {
            ItemStack armor2 = AntiDuplication.getInstance().applyAntiDupe(armor[2]);
            livingEntity.getEquipment().setChestplate(armor2);
            this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor2));
        }
        if (enumMonster == EnumMonster.FrozenSkeleton) {
            livingEntity.getEquipment().setHelmet(SkullTextures.FROZEN_SKELETON.getSkull());
        } else {
            livingEntity.getEquipment().setHelmet(SkullTextures.SKELETON.getSkull());
        }
    }

    public void setWeapon(int tier) {

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

    protected String getCustomEntityName() {
        return this.enumMonster.name;
    }

    @Override
    public void onMonsterAttack(Player p) {
        // TODO Auto-generated type stub

    }

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }

    @Override
    public void onMonsterDeath(Player killer) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), enumMonster, this.getBukkitEntity(), killer);
        });
    }

    @Override
    public EnumMonster getEnum() {
        return this.enumMonster;
    }
}
