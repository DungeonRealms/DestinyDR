package net.dungeonrealms.game.world.entity.type.monster.type.melee;

import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRZombie;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class MeleeZombie extends DRZombie {
	
    public MeleeZombie(World world, EnumMonster type, int tier) {
        super(world, type, tier);
    }

    public MeleeZombie(World world) {
        super(world);
    }
}
