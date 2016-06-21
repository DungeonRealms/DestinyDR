package net.dungeonrealms.game.world.entities.types.pets;

import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.minecraft.server.v1_9_R2.EntityWolf;
import net.minecraft.server.v1_9_R2.World;

import java.util.UUID;

/**
 * Created by Kieran on 9/5/2015.
 */
public class Wolf extends EntityWolf {

    private String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;

    public Wolf(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
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
        this.setSitting(false);

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
    }

    @Override
    protected void r() {
    }

    public Wolf(World world) {
        super(world);
    }
}
