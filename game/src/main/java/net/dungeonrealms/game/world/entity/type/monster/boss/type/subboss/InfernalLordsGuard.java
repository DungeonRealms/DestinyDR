package net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.MeleeWitherSkeleton;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.inventory.ItemStack;

/**
 * Infernal Lords Guard SubBoss.
 * <p>
 * Redone on April 28th, 2017.
 *
 * @author Kneesnap
 */
public class InfernalLordsGuard extends MeleeWitherSkeleton implements DungeonBoss {

    public InfernalLordsGuard(World world) {
        super(world);
        this.fireProof = false;
        this.setOnFire(Integer.MAX_VALUE);
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(65);
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.4F);

        setGear();
    }


    @Override
    public ItemStack getWeapon() {
        ItemWeapon weapon = new ItemWeapon(ItemType.POLEARM, ItemType.SWORD);
        weapon.setRarity(ItemRarity.COMMON);
        return makeItem(weapon);
    }


    @Override
    public void n() {
        super.n();
        this.setOnFire(Integer.MAX_VALUE);
    }

    @Override
    public void setGear() {
        super.setGear();
        ItemArmor am = new ItemArmor();
        am.setTier(4).setRarity(ItemRarity.COMMON);
        getBukkit().getEquipment().setArmorContents(am.generateArmorSet());
    }


    @Override
    protected void r() {
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalRandomStroll(this, 1.2D, 20));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false, new Class[0]));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }


    @Override
    public BossType getBossType() {
        return BossType.InfernalGuard;
    }

}
