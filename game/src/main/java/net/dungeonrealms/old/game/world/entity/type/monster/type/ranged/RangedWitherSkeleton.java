package net.dungeonrealms.old.game.world.entity.type.monster.type.ranged;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.old.game.anticheat.AntiDuplication;
import net.dungeonrealms.old.game.world.entity.EnumEntityType;
import net.dungeonrealms.old.game.world.entity.type.monster.base.DRWitherSkeleton;
import net.dungeonrealms.old.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.old.game.world.item.DamageAPI;
import net.dungeonrealms.old.game.world.item.Item;
import net.dungeonrealms.old.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class RangedWitherSkeleton extends DRWitherSkeleton implements IRangedEntity {
    private int tier;

    public RangedWitherSkeleton(World world, EnumMonster monsterType, EnumEntityType entityType, int tier) {
        super(world, monsterType, tier, entityType);
        this.tier = tier;
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        setWeapon(tier);
        this.setSize(0.7F, 2.4F);
        this.fireProof = true;
        this.setSkeletonType(1);
    }

    public RangedWitherSkeleton(World world) {
        super(world);
    }

    @Override
    protected void setStats() {
    }

    @Override
    public void collide(Entity e) {
    }

    @Override
    public void setWeapon(int tier) {
        ItemStack weapon = getTierWeapon(tier);
        this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        livingEntity.getEquipment().setItemInMainHand(weapon);

    }

    private ItemStack getTierWeapon(int tier) {
        ItemStack item = new ItemGenerator().setType(Item.ItemType.BOW).setRarity(GameAPI.getItemRarity(false)).setTier(Item.ItemTier.getByTier(tier)).generateItem().getItem();
        AntiDuplication.getInstance().applyAntiDupe(item);
        return item;
    }

    @Override
    public void a(EntityLiving entityliving, float f) {
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = this.getEquipment(EnumItemSlot.MAINHAND);
        NBTTagCompound tag = nmsItem.getTag();
        DamageAPI.fireArrowFromMob((CraftLivingEntity) this.getBukkitEntity(), tag, (CraftLivingEntity) entityliving.getBukkitEntity());
    }
}
