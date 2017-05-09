package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 09-Jun-16.
 */
public class DRGhast extends EntityGhast implements DRMonster {

    public DRGhast(World world) {
        super(world);
    }

    @Override
    public ItemStack getWeapon() {
    	return makeItem(new ItemWeaponStaff());
    }

    @Override
    public void collide(Entity e) {}

    @Override
    public EnumMonster getEnum() {
        return null;
    }

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }
}
