package net.dungeonrealms.entities.types.pets;

import java.util.UUID;

import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.mastery.MetadataUtils;
import net.minecraft.server.v1_8_R3.EntityCaveSpider;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Kieran on 9/5/2015.
 */
public class CaveSpider extends EntityCaveSpider {

    private String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;

    public CaveSpider(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
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

    public CaveSpider(World world) {
        super(world);
    }
}
