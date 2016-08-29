package net.dungeonrealms.game.world.entity.type.pet;

import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.minecraft.server.v1_9_R2.EntityPigZombie;
import net.minecraft.server.v1_9_R2.World;

import java.util.UUID;

/**
 * Created by Kieran on 9/5/2015.
 */
public class BabyZombiePig extends EntityPigZombie {

    private String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;

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

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
    }

    public BabyZombiePig(World world) {
        super(world);
    }

    @Override
    protected void r() {
    }
}
