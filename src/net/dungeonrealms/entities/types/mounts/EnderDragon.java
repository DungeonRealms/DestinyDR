package net.dungeonrealms.entities.types.mounts;

import java.util.UUID;

import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.mastery.MetadataUtils;
import net.minecraft.server.v1_8_R3.EntityEnderDragon;
import net.minecraft.server.v1_8_R3.World;

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
