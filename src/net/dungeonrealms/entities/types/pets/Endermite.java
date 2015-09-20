package net.dungeonrealms.entities.types.pets;

import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mastery.MetadataUtils;
import net.minecraft.server.v1_8_R3.EntityEndermite;
import net.minecraft.server.v1_8_R3.World;

import java.util.UUID;

/**
 * Created by Kieran on 9/5/2015.
 */
public class Endermite extends EntityEndermite {

    public String mobName;
    public UUID ownerUUID;
    public EnumEntityType entityType;

    public Endermite(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
        super(world);
        this.mobName = mobName;
        this.ownerUUID = ownerUUID;
        this.entityType = entityType;
        this.getBukkitEntity().setCustomNameVisible(true);
        this.getBukkitEntity().setCustomName(mobName);
        this.canPickUpLoot = false;
        this.persistent = true;

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
    }

    public Endermite(World world) {
        super(world);
    }
}
