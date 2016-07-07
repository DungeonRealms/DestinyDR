package net.dungeonrealms.game.world.entities.types.monsters.BowMobs;

import net.dungeonrealms.API;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRZombie;
import net.dungeonrealms.game.world.entities.types.monsters.pathfinders.PathFinderShootBow;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class RangedZombie extends DRZombie implements IRangedEntity {

    public RangedZombie(World world, EnumMonster monsterType, int tier) {
        super(world, monsterType, tier, EnumEntityType.HOSTILE_MOB);
        this.tier = tier;
        setWeapon(tier);
        clearGoalSelectors();
        this.goalSelector.a(0, new PathfinderGoalRandomStroll(this, .6F));
        this.goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(2, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(4, new PathFinderShootBow(this, 1.0D, 20, 15.0F));
    }

    public RangedZombie(World world) {
        super(world);
    }

    @Override
    protected void setStats() {
    }


    @Override
    public void setWeapon(int tier) {
        ItemStack weapon = getTierWeapon(tier);
        this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        livingEntity.getEquipment().setItemInMainHand(weapon);

    }

    @Override
    public EnumMonster getEnum() {
        return null;
    }

    private ItemStack getTierWeapon(int tier) {
        ItemStack item = new ItemGenerator().setType(Item.ItemType.BOW).setRarity(API.getItemRarity(false)).setTier(Item.ItemTier.getByTier(tier)).generateItem().getItem();
        AntiCheat.getInstance().applyAntiDupe(item);
        return item;
    }

    @Override
    public void a(EntityLiving entityLiving, float v) {
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = this.getEquipment(EnumItemSlot.MAINHAND);
        NBTTagCompound tag = nmsItem.getTag();
        DamageAPI.fireArrowFromMob((CraftLivingEntity) this.getBukkitEntity(), tag, (CraftLivingEntity) entityLiving.getBukkitEntity());
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
