package net.dungeonrealms.game.world.entities.types.pets;

import java.util.UUID;

import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.minecraft.server.v1_8_R3.EntityOcelot;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Kieran on 9/5/2015.
 */
public class Ocelot extends EntityOcelot {

    private String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;

    public Ocelot(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
        super(world);
        this.mobName = mobName;
        this.ownerUUID = ownerUUID;
        this.entityType = entityType;
        this.getBukkitEntity().setCustomNameVisible(true);
        this.getBukkitEntity().setCustomName(mobName);
        this.canPickUpLoot = false;
        this.persistent = true;
        this.setAge(0);
        this.ageLocked = true;

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
    }

    public Ocelot(World world) {
        super(world);
    }
}
