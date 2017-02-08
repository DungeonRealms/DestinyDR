package net.dungeonrealms.game.world.entity.type.monster.base;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kieran Quigley (Proxying) on 09-Jun-16.
 */
public class DRWitch extends EntityWitch implements DRMonster {

    EnumMonster monster;
    int tier;
    @Getter
    ItemStack weapon;
    @Getter
    protected Map<String, Integer[]> attributes = new HashMap<>();

    public DRWitch(World world) {
        super(world);
    }

    public DRWitch(World world, EnumMonster mon, int tier) {
        super(world);
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(20d);
        this.getAttributeInstance(GenericAttributes.c).setValue(1.00d);
        weapon = getTierWeapon(tier);
        setArmor(tier);
        monster = mon;
        String customName = mon.getPrefix() + " " + mon.name + " " + mon.getSuffix() + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(2, new PathfinderGoalArrowAttack(this, 1.0D, 60, 10.0F));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));

        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(monster.getSkullItem(monster)));
        livingEntity.getEquipment().setHelmet(monster.getSkullItem(monster));
        this.noDamageTicks = 0;
        this.maxNoDamageTicks = 0;
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
        livingEntity.getEquipment().setItemInMainHand(weapon);
        this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
        this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.DEVIL.getSkull()));
        livingEntity.getEquipment().setHelmet(SkullTextures.DEVIL.getSkull());
    }

    private ItemStack getTierWeapon(int tier) {
        ItemStack item = new ItemGenerator().setTier(Item.ItemTier.getByTier(tier)).setType(Item.ItemType.STAFF)
                .setRarity(GameAPI.getItemRarity(false)).generateItem().getItem();
        AntiDuplication.getInstance().applyAntiDupe(item);
        return item;
    }

    @Override
    public void collide(Entity e) {
    }

    @Override
    public void onMonsterAttack(Player p) {
    }

    @Override
    public void onMonsterDeath(Player killer) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), monster, this.getBukkitEntity(), killer);
        });
    }

    @Override
    public EnumMonster getEnum() {
        return null;
    }

    //    @Override
//    public void a(EntityLiving entity, float f) {
//        Projectile projectile = ((CraftLivingEntity) this.getBukkitEntity()).launchProjectile(ThrownPotion.class);
//        MetadataUtils.registerProjectileMetadata(this.getAttributes(), CraftItemStack.asNMSCopy(weapon).getTag(),
//                projectile);
//
//    }
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
            MetadataUtils.registerProjectileMetadata(this.getAttributes(), CraftItemStack.asNMSCopy(weapon).getTag(), (Projectile)var13.getBukkitEntity());
        }
    }

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }
}
