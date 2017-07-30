package net.dungeonrealms.game.world.entity.type.monster.base;

import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.mechanic.ReflectionAPI;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.pathfinders.PathfinderGoalBlazeFireball;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class DRBlaze extends EntityBlaze implements DRMonster {


    @Getter
    public Set<DRMagma> magmaCubes = new ConcurrentSet<>();

    protected DRBlaze(World world) {
        super(world);
    }

    @Override
    protected void initAttributes() {
        super.initAttributes();
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(25D);
    }

    @Override
    public EnumMonster getEnum() {
        return EnumMonster.Blaze;
    }

    @Override
    public ItemStack getWeapon() {
        return makeItem(new ItemWeaponStaff());
    }

    private long lastChecked;

    @Override
    protected void r() {
        super.r();
        try {
            Field bSet = ReflectionAPI.getDeclaredField(goalSelector.getClass(), "b");
            LinkedHashSet<?> b = (LinkedHashSet) bSet.get(goalSelector);
            for (Object o : new LinkedHashSet<>(b)) {
                Field goal = ReflectionAPI.getDeclaredField(o.getClass(), "a");
                PathfinderGoal pathGoal = (PathfinderGoal) goal.get(o);
                if (pathGoal.getClass().getName().contains("PathfinderGoalBlazeFireball")) {
                    //Remove this?
                    b.remove(o);
                    bSet.set(goalSelector, b);
                }
            }
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }
        this.goalSelector.a(4, new PathfinderGoalBlazeFireball(this));
    }

    @Override
    public void n() {
        super.n();

        if (lastChecked > System.currentTimeMillis()) return;
        lastChecked = System.currentTimeMillis() + 500;
        for (DRMagma magma : magmaCubes) {
            if (!magma.isAlive() || magma.dead)
                magmaCubes.remove(magma);
        }
    }

    @Override
    public void collide(Entity e) {
    }

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }
}