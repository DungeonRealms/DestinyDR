package net.dungeonrealms.entities.types.mounts;

import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.mastery.MetadataUtils;
import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.World;

import java.util.UUID;

/**
 * Created by Kieran on 9/18/2015.
 */
public class Horse extends EntityHorse {

    private UUID ownerUUID;
    private EnumEntityType entityType;
    private int horseType;
    private double horseSpeed;

    public Horse(World world, int horseType, double horseSpeed, UUID ownerUUID, EnumEntityType entityType) {
        super(world);
        this.ownerUUID = ownerUUID;
        this.entityType = entityType;
        this.horseType = horseType;
        this.horseSpeed = horseSpeed;
        this.getBukkitEntity().setCustomNameVisible(true);
        this.canPickUpLoot = false;
        this.persistent = true;
        this.setType(horseType);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(horseSpeed);
        this.setOwnerUUID(ownerUUID.toString());

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
    }

    public Horse(World world) {
        super(world);
    }
}
