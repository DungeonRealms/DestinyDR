package net.dungeonrealms.game.world.entities.types.monsters.MeleeMobs;

import net.dungeonrealms.API;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRSkeleton;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by Kieran Quigley (Proxying) on 15-Jun-16.
 */
public class MeleeSkeleton extends DRSkeleton {

    public MeleeSkeleton(World world, EnumMonster monsterType , EnumEntityType entityType, int tier) {
        super(world, monsterType, tier, entityType);
        this.entityType = entityType;
        setWeapon(tier);
    }

    public MeleeSkeleton(World world) {
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
        Item.ItemType itemType;
        switch (new Random().nextInt(3)) {
            case 0:
                itemType = Item.ItemType.SWORD;
                break;
            case 1:
                itemType = Item.ItemType.POLEARM;
                break;
            case 2:
                itemType = Item.ItemType.AXE;
                break;
            default:
                itemType = Item.ItemType.SWORD;
                break;
        }
        ItemStack item = new ItemGenerator().setType(itemType).setRarity(API.getItemRarity(false))
                .setTier(Item.ItemTier.getByTier(tier)).generateItem().getItem();
        AntiCheat.getInstance().applyAntiDupe(item);
        return item;
    }

    @Override
    public void a(EntityLiving entityliving, float f) {
    }

    @Override
    public void setStats() {
    }

    @Override
    public EnumMonster getEnum() {
        return this.monsterType;
    }
}
