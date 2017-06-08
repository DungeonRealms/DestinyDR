package net.dungeonrealms.game.world.entity.type.monster.base;

import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.EntityGiantZombie;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.entity.Giant;

/**
 * Created by Rar349 on 6/7/2017.
 */
public class DRGiant extends EntityGiantZombie implements DRMonster {

    public DRGiant(World world) {
        super(world);
    }


    @Override
    public EnumMonster getEnum() {
        return EnumMonster.Giant;
    }

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        //Test for EnderPearl TP Cancel.
    }
}
