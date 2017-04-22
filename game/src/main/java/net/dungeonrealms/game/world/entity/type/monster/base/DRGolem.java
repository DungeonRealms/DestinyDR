package net.dungeonrealms.game.world.entity.type.monster.base;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public abstract class DRGolem extends EntityIronGolem implements DRMonster {

    protected String name;
    protected EnumEntityType entityType;
    protected EnumMonster monsterType;
    public int tier;
    @Getter
    protected Map<String, Integer[]> attributes = new HashMap<>();

    protected DRGolem(World world, EnumMonster monsterType, int tier, EnumEntityType type) {
        this(world);
        this.monsterType = monsterType;
        this.name = monsterType.name;
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(20d);
        this.getAttributeInstance(GenericAttributes.c).setValue(1.00d);
        setArmor(tier);
        setStats();
        String customName = monsterType.getPrefix().trim() + " " + monsterType.name.trim() + " " + monsterType.getSuffix().trim() + " ";
        this.setCustomName(customName);
        EntityInsentient entityInsentient = this;
        entityInsentient.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
        LivingEntity livingEntity = (LivingEntity) entityInsentient.getBukkitEntity();
        this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(monsterType.getSkullItem(monsterType)));
        livingEntity.getEquipment().setHelmet(monsterType.getSkullItem(monsterType));
        this.noDamageTicks = 0;
        this.maxNoDamageTicks = 0;
        this.setPlayerCreated(false);
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    protected DRGolem(World world) {
        super(world);
    }

    protected abstract void setStats();

    @Override
    public EnumMonster getEnum(){
    	return this.monsterType;
    }

    public void setArmor(int tier) {
        org.bukkit.inventory.ItemStack[] armor = GameAPI.getTierArmor(tier);
        // weapon, boots, legs, chest, helmet/head
        EntityInsentient entityInsentient = this;
        LivingEntity livingEntity = (LivingEntity) entityInsentient.getBukkitEntity();
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

    public void setWeapon(int tier) {
    }


    @Override
    public void onMonsterAttack(Player p) {
    }

    @Override
    public void collide(Entity e) {}

    @Override
    public void onMonsterDeath(Player killer) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
            this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), this.getBukkitEntity(), killer);
        });
    }

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }
}
