package net.dungeonrealms.old.game.world.entity.type.monster.base;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.old.game.anticheat.AntiDuplication;
import net.dungeonrealms.old.game.miscellaneous.SkullTextures;
import net.dungeonrealms.old.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.old.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.old.game.world.item.Item;
import net.dungeonrealms.old.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }
}
