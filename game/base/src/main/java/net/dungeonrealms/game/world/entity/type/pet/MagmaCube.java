package net.dungeonrealms.game.world.entity.type.pet;

import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.EntityMagmaCube;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Kieran Quigley (Proxying) on 29-May-16.
 */
public class MagmaCube extends EntityMagmaCube {

    private String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;
    private Player target;

    public MagmaCube(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
        super(world);
        this.mobName = mobName;
        this.ownerUUID = ownerUUID;
        this.entityType = entityType;
        this.getBukkitEntity().setCustomNameVisible(true);
        this.getBukkitEntity().setCustomName(mobName);
        this.setSize(1);
        this.canPickUpLoot = false;
        this.persistent = true;

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
    }

    public MagmaCube(World world) {
        super(world);
    }


    @Override
    public void d(EntityHuman entityhuman) {
    }

    @Override
    protected void d(EntityLiving entityliving) {
    }

    @Override
    protected void r() {
    }
}
