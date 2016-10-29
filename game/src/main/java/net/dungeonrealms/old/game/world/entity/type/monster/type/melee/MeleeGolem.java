package net.dungeonrealms.old.game.world.entity.type.monster.type.melee;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.old.game.anticheat.AntiDuplication;
import net.dungeonrealms.old.game.world.entity.EnumEntityType;
import net.dungeonrealms.old.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.old.game.world.entity.type.monster.base.DRGolem;
import net.dungeonrealms.old.game.world.item.Item;
import net.dungeonrealms.old.game.world.item.itemgenerator.ItemGenerator;
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
        ItemStack item = new ItemGenerator().setType(Item.ItemType.SWORD).setRarity(GameAPI.getItemRarity(false)).setTier(Item.ItemTier.getByTier(tier)).generateItem().getItem();
        AntiDuplication.getInstance().applyAntiDupe(item);
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
