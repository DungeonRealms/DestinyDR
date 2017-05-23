package net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRGolem;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.Item;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.inventory.ItemStack;

public class IceLord extends DRGolem implements DRMonster {
    public IceLord(World world) {
        super(world);
        setTier(4);
        setGear();

        EntityAPI.clearAI(goalSelector, targetSelector);
        this.r();
    }

    @Override
    public void setGear() {
        super.setGear();

        ItemArmor am = new ItemArmor();
        am.setTier(4).setRarity(Item.ItemRarity.UNIQUE);
        getBukkit().getEquipment().setArmorContents(am.generateArmorSet());
    }

    @Override
    protected void r() {
        this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this, 1.1D, true));
        this.goalSelector.a(2, new PathfinderGoalMoveTowardsTarget(this, 1.2D, 32.0F));
        this.goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 0.6D));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(2, new PathfinderGoalHurtByTarget(this, false, new Class[0]));
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    @Override
    public ItemStack getWeapon() {
        return makeItem(new ItemWeapon(ItemType.POLEARM).setTier(getTier()).setRarity(Item.ItemRarity.UNIQUE));
    }
}
