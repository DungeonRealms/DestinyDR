package net.dungeonrealms.game.world.entity.type;

import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.minecraft.server.v1_9_R2.EntityEnderCrystal;
import net.minecraft.server.v1_9_R2.World;

/**
 * Created by Kieran on 9/18/2015.
 */
public class EnderCrystal extends EntityEnderCrystal {

    private EnumEntityType entityType;

    public EnderCrystal(World world, EnumEntityType entityType) {
        super(world);
        this.entityType = entityType;

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
    }

    public EnderCrystal(World world) {
        super(world);
    }
}
