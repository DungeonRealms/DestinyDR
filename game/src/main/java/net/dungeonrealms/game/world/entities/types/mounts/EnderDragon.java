package net.dungeonrealms.game.world.entities.types.mounts;

import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.minecraft.server.v1_9_R2.EntityEnderDragon;
import net.minecraft.server.v1_9_R2.World;

import java.util.UUID;

/**
 * Created by Kieran on 9/19/2015.
 */
public class EnderDragon extends EntityEnderDragon {

    public String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;

    public EnderDragon(World world, UUID ownerUUID, EnumEntityType entityType) {
        super(world);
        this.ownerUUID = ownerUUID;
        this.entityType = entityType;
        this.getBukkitEntity().setCustomNameVisible(true);
        this.canPickUpLoot = false;
        this.persistent = true;

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
    }

    public EnderDragon(World world) {
        super(world);
    }
}
