package net.dungeonrealms.game.world.entity.type.pet;

import com.google.common.base.Optional;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.event.entity.EntityTargetEvent;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Created by Rar349 on 4/30/2017.
 */
public class Enderman extends EntityEnderman {

    private String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;

    public Enderman(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
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

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        return false;
    }

    @Override
    public void setGoalTarget(@Nullable EntityLiving entityliving) {

    }

    @Override
    protected boolean db() {
        return false;
    }

    @Override
    protected boolean a(Entity entity) {
        return false;
    }

    @Override
    public void setCarried(@Nullable IBlockData iblockdata) {

    }

    public Enderman(World world) {
        super(world);
    }

    @Override
    protected void r() {
    }
}
