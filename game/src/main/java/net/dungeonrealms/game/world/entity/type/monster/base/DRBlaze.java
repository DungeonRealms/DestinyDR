package net.dungeonrealms.game.world.entity.type.monster.base;

import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityBlaze;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.inventory.ItemStack;

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