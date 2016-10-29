package net.dungeonrealms.old.game.world.entity.type.pet;

import net.dungeonrealms.old.game.mastery.MetadataUtils;
import net.dungeonrealms.old.game.world.entity.EnumEntityType;
import net.minecraft.server.v1_9_R2.EntityCreeper;
import net.minecraft.server.v1_9_R2.World;

import java.util.UUID;

/**
 * Created by Kieran Quigley (Proxying) on 26-Jun-16.
 */
public class Creeper extends EntityCreeper {

    private String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;

    public Creeper(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
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

    public Creeper(World world) {
        super(world);
    }

    @Override
    protected void r() {
    }

    /*@Override
    public void m() {
    }*/
}
