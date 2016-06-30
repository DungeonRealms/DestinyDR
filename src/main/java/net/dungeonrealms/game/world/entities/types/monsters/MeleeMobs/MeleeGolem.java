package net.dungeonrealms.game.world.entities.types.monsters.MeleeMobs;

import net.dungeonrealms.API;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRGolem;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class MeleeGolem extends DRGolem {

    public MeleeGolem(World world, int tier, EnumEntityType entityType) {
        super(world, EnumMonster.Golem, tier, entityType);
        setWeapon(tier);
    }

    public MeleeGolem(World world) {
        super(world);
    }

    @Override
    public void setWeapon(int tier) {
        ItemStack weapon = getTierWeapon(tier);
        this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        livingEntity.getEquipment().setItemInMainHand(weapon);
        this.setPlayerCreated(false);
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    private ItemStack getTierWeapon(int tier) {
        ItemStack item = new ItemGenerator().setType(Item.ItemType.SWORD).setRarity(API.getItemRarity(false)).setTier(Item.ItemTier.getByTier(tier)).generateItem().getItem();
        AntiCheat.getInstance().applyAntiDupe(item);
        return item;
    }

	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}


    @Override
    protected void setStats() {

    }

}
