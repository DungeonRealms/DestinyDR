package net.dungeonrealms.game.world.entity.type.monster.type.melee;

import net.dungeonrealms.game.world.entity.type.monster.base.DREnderman;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Kieran Quigley (Proxying) on 21-Jun-16.
 */
public class MeleeEnderman extends DREnderman {

    public MeleeEnderman(World world, int tier) {
        super(world, tier);

        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(40);
    }

    public MeleeEnderman(World world) {
        super(world);
    }
}
