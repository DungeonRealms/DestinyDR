package net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRZombie;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class StaffZombie extends DRZombie implements IRangedEntity {

    public StaffZombie(World world, EnumMonster mons, int tier) {
        super(world, mons, tier, EnumEntityType.HOSTILE_MOB);
        this.tier = tier;
        setWeapon(tier);

        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(25D);
        clearGoalSelectors();
        this.goalSelector.a(0, new PathfinderGoalArrowAttack(this, 1.3D, 15, 40, 15.0F));
        this.goalSelector.a(1, new PathfinderGoalRandomStroll(this, .7F));
        this.goalSelector.a(2, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(3, new PathfinderGoalRandomLookaround(this));

//        this.targetSelector.a(0, new PathfinderGoalArrowAttack(this, 1.0D, 15, 40, 15.0F));
        this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    public StaffZombie(World world) {
        super(world);
        clearGoalSelectors();
        this.goalSelector.a(0, new PathfinderGoalRandomStroll(this, .6F));
        this.goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(2, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new PathfinderGoalArrowAttack(this, 1.0D, 20, 60, 15.0F));
    }

    @Override
    public void setWeapon(int tier) {
        org.bukkit.inventory.ItemStack weapon = new ItemGenerator().setType(Item.ItemType.STAFF).setTier(Item.ItemTier.getByTier(tier)).setRarity(GameAPI.getItemRarity(false)).generateItem().getItem();
        this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
        ((LivingEntity) this.getBukkitEntity()).getEquipment().setItemInMainHand(weapon);
    }


    @Override
    protected void r() {}

    @Override
    protected void o() {
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    @Override
    public void setStats() {

    }

    @Override
    public EnumMonster getEnum() {
        return this.monsterType;
    }

    @Override
    public void a(EntityLiving entity, float f) {
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = this.getEquipment(EnumItemSlot.MAINHAND);
        NBTTagCompound tag = nmsItem.getTag();
        Projectile proj = DamageAPI.fireStaffProjectileMob((CraftLivingEntity) this.getBukkitEntity(), tag, (CraftLivingEntity) entity.getBukkitEntity());
//        if(proj != null){
//            proj.setVelocity(proj.getVelocity().multiply(2));
//        }
    }

    @Override
    public void collide(Entity e) {
        if(e != null && e instanceof Projectile){
            if(!(((Projectile)e).getShooter() instanceof Player))return;
        }
        super.collide(e);
    }

    private void clearGoalSelectors() {
        try {
            Field a = PathfinderGoalSelector.class.getDeclaredField("b");
            Field b = PathfinderGoalSelector.class.getDeclaredField("c");
            a.setAccessible(true);
            b.setAccessible(true);
            ((LinkedHashSet) a.get(this.goalSelector)).clear();
            ((LinkedHashSet) b.get(this.goalSelector)).clear();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
