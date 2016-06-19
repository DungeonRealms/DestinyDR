package net.dungeonrealms.game.world.entities.types.monsters.MeleeMobs;

import net.dungeonrealms.API;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRWitherSkeleton;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
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

    private ItemStack getTierWeapon(int tier) {
        net.dungeonrealms.game.world.items.Item.ItemType itemType;
        switch (new Random().nextInt(3)) {
            case 0:
                itemType = net.dungeonrealms.game.world.items.Item.ItemType.SWORD;
                break;
            case 1:
                itemType = net.dungeonrealms.game.world.items.Item.ItemType.POLEARM;
                break;
            case 2:
                itemType = net.dungeonrealms.game.world.items.Item.ItemType.AXE;
                break;
            default:
                itemType = net.dungeonrealms.game.world.items.Item.ItemType.SWORD;
                break;
        }
        ItemStack item = new ItemGenerator().setType(itemType).setRarity(API.getItemRarity(false))
                .setTier(Item.ItemTier.getByTier(tier)).generateItem().getItem();
        AntiCheat.getInstance().applyAntiDupe(item);
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
