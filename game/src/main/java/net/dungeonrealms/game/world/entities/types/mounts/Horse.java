package net.dungeonrealms.game.world.entities.types.mounts;

import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.minecraft.server.v1_9_R2.EntityHorse;
import net.minecraft.server.v1_9_R2.EnumHorseType;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;

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
        this.setTame(true);
        this.canPickUpLoot = false;
        this.persistent = true;
        this.setType(EnumHorseType.a(horseType));
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(horseSpeed);
        this.setOwnerUUID(ownerUUID);
        this.setTemper(100);

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
    }

    public Horse(World world) {
        super(world);
    }
}
