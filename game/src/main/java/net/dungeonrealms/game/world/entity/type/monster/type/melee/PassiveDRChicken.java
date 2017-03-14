package net.dungeonrealms.game.world.entity.type.monster.type.melee;

import lombok.Getter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.pathfinders.PathfinderPassiveMeleeAttack;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PassiveDRChicken extends EntityChicken implements DRMonster {
    int tier;
    @Getter
    protected Map<String, Integer[]> attributes = new HashMap<>();

    public PassiveDRChicken(World world, int tier) {
        super(world);
        this.tier = tier;
        setArmor(tier);
        setWeapon();
    }

    @Override
    protected void r() {
        this.targetSelector.a(0, new PathfinderPassiveMeleeAttack(this, 1.2D, true));
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
//        this.goalSelector.a(1, new PathfinderGoalPanic(this, 1.4D));a
//        this.goalSelector.a(2, new PathfinderGoalBreed(this, 1.0D));
//        this.goalSelector.a(3, new PathfinderGoalTempt(this, 1.0D, false, bE));
//        this.goalSelector.a(4, new PathfinderGoalFollowParent(this, 1.1D));
        this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, 0.9D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
    }

    public void setWeapon() {
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        livingEntity.getEquipment().setItemInMainHand(getTierWeapon(tier));
    }

    private org.bukkit.inventory.ItemStack getTierWeapon(int tier) {
        net.dungeonrealms.game.world.item.Item.ItemType itemType;
        switch (new Random().nextInt(3)) {
            case 0:
                itemType = net.dungeonrealms.game.world.item.Item.ItemType.SWORD;
                break;
            case 1:
                itemType = net.dungeonrealms.game.world.item.Item.ItemType.POLEARM;
                break;
            case 2:
                itemType = net.dungeonrealms.game.world.item.Item.ItemType.AXE;
                break;
            default:
                itemType = net.dungeonrealms.game.world.item.Item.ItemType.SWORD;
                break;
        }
        org.bukkit.inventory.ItemStack item = new ItemGenerator().setType(itemType).setRarity(GameAPI.getItemRarity(false))
                .setTier(net.dungeonrealms.game.world.item.Item.ItemTier.getByTier(tier)).generateItem().getItem();
        AntiDuplication.getInstance().applyAntiDupe(item);
        return item;
    }

    public void setArmor(int tier) {
        org.bukkit.inventory.ItemStack[] armor = GameAPI.getTierArmor(tier);
        // weapon, boots, legs, chest, helmet/head
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        boolean armorMissing = false;
        int chance = 6 + tier;
        if (tier >= 3 || random.nextInt(10) <= chance) {
            org.bukkit.inventory.ItemStack armor0 = AntiDuplication.getInstance().applyAntiDupe(armor[0]);
            livingEntity.getEquipment().setBoots(armor0);
            this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor0));
        } else {
            armorMissing = true;
        }
        if (tier >= 3 || random.nextInt(10) <= chance || armorMissing) {
            org.bukkit.inventory.ItemStack armor1 = AntiDuplication.getInstance().applyAntiDupe(armor[1]);
            livingEntity.getEquipment().setLeggings(armor1);
            this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor1));
            armorMissing = false;
        } else {
            armorMissing = true;
        }
        if (tier >= 3 || random.nextInt(10) <= chance || armorMissing) {
            org.bukkit.inventory.ItemStack armor2 = AntiDuplication.getInstance().applyAntiDupe(armor[2]);
            livingEntity.getEquipment().setChestplate(armor2);
            this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor2));
        }
    }

    @Override
    public void onMonsterAttack(Player p) {
    }

    @Override
    public void onMonsterDeath(Player killer) {
    }

//    private int hitTicks = 1;
//
//    @Override
//    public void collide(Entity entity) {
//        //Attack target?
////        System.out.println("Colliding: " + entity);
////        if (entity instanceof EntityHuman && this.getGoalTarget() != null && this.getGoalTarget().getUniqueID().equals(entity.getUniqueID())) {
////            EntityHuman human = (EntityHuman) entity;
////            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(getBukkitEntity(), entity.getBukkitEntity(), EntityDamageEvent.DamageCause.ENTITY_ATTACK, 20);
////            Bukkit.getPluginManager().callEvent(event);
////            if (!event.isCancelled()) {
////                HealthHandler.getInstance().handlePlayerBeingDamaged((Player) human.getBukkitEntity(), getBukkitEntity(), 5, 0, 0);
////                human.getBukkitEntity().damage(0, getBukkitEntity());
////            }
////        }
//        super.collide(entity);
//    }

    @Override
    public EnumMonster getEnum() {
        return EnumMonster.PassiveChicken;
    }

}
