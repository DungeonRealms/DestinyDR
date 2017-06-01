package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.mechanic.ReflectionAPI;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.entity.util.pathfinders.PathfinderGoalMeleeAttackWell;
import net.minecraft.server.v1_9_R2.*;

public abstract class DRSkeleton extends EntitySkeleton implements DRMonster {
    protected EnumMonster monsterType;

    private PathfinderGoalBowShoot goalShoot;
    private PathfinderGoalMeleeAttackWell newAttack;

    protected DRSkeleton(World world, EnumMonster type) {
        this(world);
        setMonster(type);
        getGoals();
    }

    public DRSkeleton(World world) {
        super(world);
        getGoals();
    }

    public void getGoals() {
        goalShoot = (PathfinderGoalBowShoot) ReflectionAPI.getObjectFromField("c", EntitySkeleton.class, this);
        newAttack = new PathfinderGoalMeleeAttackWell(this, 1.1D, false);
    }

    @Override //This is the shoot arrow method I believe.
    public abstract void a(EntityLiving entityliving, float f);

    @Override
    public void collide(Entity e) {
    }

    @Override
    public void setMonster(EnumMonster m) {
        this.monsterType = m;
    }

    @Override
    public EnumMonster getEnum() {
        return this.monsterType;
    }

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }

    @Override
    protected void r() {
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(3, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(2, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(4, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new PathfinderGoalHurtByTarget(this, false, EntityHuman.class));
        this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
    }

    @Override
    public void o() {
        if (goalShoot == null) {
            getGoals();
        }
        if (this.world != null && !this.world.isClientSide) {
            EntityAPI.clearAI(goalSelector, targetSelector);
            this.r();
            ItemStack itemstack = this.getItemInMainHand();
            //Only give them this bow one if this have a bow.
            if (itemstack != null && itemstack.getItem().equals(Items.BOW)) {
                byte b0 = 20;
                this.goalShoot.b(b0);
                this.goalSelector.a(4, this.goalShoot);
            } else {
                this.goalSelector.a(1, newAttack);
            }
        }
    }
}
