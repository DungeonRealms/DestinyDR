package net.dungeonrealms.entities.types;

import net.dungeonrealms.entities.Entities;
import net.dungeonrealms.enums.EnumEntityType;
import net.minecraft.server.v1_8_R3.EntityPigZombie;
import net.minecraft.server.v1_8_R3.World;

import java.util.UUID;

/**
 * Created by Kieran on 9/5/2015.
 */
public class BabyZombiePig extends EntityPigZombie {

    public String mobName;
    public UUID ownerUUID;
    public EnumEntityType entityType;

    public BabyZombiePig(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
        super(world);
        this.mobName = mobName;
        this.ownerUUID = ownerUUID;
        this.entityType = entityType;
        this.getBukkitEntity().setCustomNameVisible(true);
        this.getBukkitEntity().setCustomName(mobName);
        this.canPickUpLoot = false;
        this.setBaby(true);
        this.persistent = true;

        Entities.getInstance().registerEntityMetadata(this, this.entityType, 0, 0);
    }
}
