package net.dungeonrealms.game.world.entities.types.pets;

import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.minecraft.server.v1_9_R2.EntityZombie;
import net.minecraft.server.v1_9_R2.World;

import java.util.UUID;

/**
 * Created by Kieran on 9/5/2015.
 */
public class BabyZombie extends EntityZombie {

    private String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;

    public BabyZombie(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
        super(world);
        this.mobName = mobName;
        this.ownerUUID = ownerUUID;
        this.entityType = entityType;
        this.getBukkitEntity().setCustomNameVisible(true);
        this.getBukkitEntity().setCustomName(mobName);
        this.canPickUpLoot = false;
        this.setBaby(true);
        this.persistent = true;

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
    }

    public BabyZombie(World world) {
        super(world);
    }

    @Override
    protected void r() {
    }
}
