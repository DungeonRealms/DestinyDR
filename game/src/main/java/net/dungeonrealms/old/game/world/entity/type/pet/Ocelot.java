package net.dungeonrealms.old.game.world.entity.type.pet;

import net.dungeonrealms.old.game.mastery.MetadataUtils;
import net.dungeonrealms.old.game.world.entity.EnumEntityType;
import net.minecraft.server.v1_9_R2.EntityOcelot;
import net.minecraft.server.v1_9_R2.World;

import java.util.UUID;

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
        this.setAgeRaw(-24000);
        this.ageLocked = true;
        this.setSitting(false);

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
    }

    public Ocelot(World world) {
        super(world);
    }

    @Override
    protected void r() {
    }
}
