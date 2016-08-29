package net.dungeonrealms.game.world.entity.type.monster.type.melee;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.base.DRWitherSkeleton;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class MeleeWitherSkeleton extends DRWitherSkeleton {
    public MeleeWitherSkeleton(World world) {
        super(world);
    }

    /**
     * @param world
     * @param tier
     */
    public MeleeWitherSkeleton(World world, int tier, EnumMonster monster, EnumEntityType entityType) {
        super(world, monster, tier, entityType);
        setWeapon(tier);
        this.setSize(0.7F, 2.4F);
        this.fireProof = true;
        this.setSkeletonType(1);
    }

    @Override
    public void setWeapon(int tier) {
        ItemStack weapon = getTierWeapon(tier);
        this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
        ((LivingEntity) this.getBukkitEntity()).getEquipment().setItemInMainHand(weapon);
    }

    @Override
    public void collide(Entity e) {}

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
        ItemStack item = new ItemGenerator().setType(itemType).setRarity(GameAPI.getItemRarity(false))
                .setTier(Item.ItemTier.getByTier(tier)).generateItem().getItem();
        AntiDuplication.getInstance().applyAntiDupe(item);
        return item;
    }

    @Override
    public EnumMonster getEnum() {
        return null;
    }


    @Override
    public void setStats() {

    }
}
