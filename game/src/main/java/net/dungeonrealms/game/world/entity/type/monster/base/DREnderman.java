package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.item.items.core.ItemWeaponSword;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.pathfinders.PathfinderGoalMeleeAttackWell;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 21-Jun-16.
 */
public abstract class DREnderman extends EntityEnderman implements DRMonster {
	
    public DREnderman(World world) {
        super(world);
    }

    @Override
    public EnumMonster getEnum(){
    	return EnumMonster.Enderman;
    }

    @Override
    public ItemStack getWeapon() {
    	return makeItem(new ItemWeaponSword());
    }

    @Override
    public void collide(Entity e) {}

    @Override
    protected void r() {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalMeleeAttackWell(this, 2.0D, false));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new PathfinderGoalHurtByTarget(this, false, new Class[0]));
        this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    @Override
    protected boolean db() {
        return false;
    }

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }
}
