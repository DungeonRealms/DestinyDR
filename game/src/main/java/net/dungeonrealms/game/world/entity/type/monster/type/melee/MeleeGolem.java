package net.dungeonrealms.game.world.entity.type.monster.type.melee;

import net.dungeonrealms.game.world.entity.type.monster.base.DRGolem;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class MeleeGolem extends DRGolem {

    public MeleeGolem(World world, int tier) {
        super(world, tier);
    }

    public MeleeGolem(World world) {
        super(world);
    }
}
