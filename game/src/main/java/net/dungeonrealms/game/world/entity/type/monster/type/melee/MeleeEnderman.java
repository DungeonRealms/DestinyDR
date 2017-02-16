package net.dungeonrealms.game.world.entity.type.monster.type.melee;

import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DREnderman;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 21-Jun-16.
 */
public class MeleeEnderman extends DREnderman {

    @Setter
    private boolean teleport = true;

    public MeleeEnderman(World world, int tier) {
        super(world, EnumMonster.Enderman, tier);
        setWeapon(tier);

        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(40);
    }

    public MeleeEnderman(World world) {
        super(world);
    }

    @Override
    public void setWeapon(int tier) {
        ItemStack weapon = getTierWeapon(tier);
        this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        livingEntity.getEquipment().setItemInMainHand(weapon);

    }

    private ItemStack getTierWeapon(int tier) {
        ItemStack item = new ItemGenerator().setType(Item.ItemType.SWORD).setRarity(GameAPI.getItemRarity(false)).setTier(Item.ItemTier.getByTier(tier)).generateItem().getItem();
        AntiDuplication.getInstance().applyAntiDupe(item);
        return item;
    }


    @Override
    protected boolean db() {
        if (teleport) {
            return super.db();
        }
        double d0 = this.locX + (this.random.nextDouble() - 0.5D) * 5.0D;
        double d1 = this.locY + (double) (this.random.nextInt(10));
        double d2 = this.locZ + (this.random.nextDouble() - 0.5D) * 5.0D;
        return this.l(d0, d1, d2);
    }

    private boolean l(double d0, double d1, double d2) {
        boolean flag = this.k(d0, d1, d2);
        if(flag) {
            this.world.a((EntityHuman)null, this.lastX, this.lastY, this.lastZ, SoundEffects.ba, this.bA(), 1.0F, 1.0F);
            this.a(SoundEffects.ba, 1.0F, 1.0F);
        }

        return flag;
    }
    @Override
    public EnumMonster getEnum() {
        return this.monsterType;
    }


    @Override
    protected void setStats() {
    }
}
