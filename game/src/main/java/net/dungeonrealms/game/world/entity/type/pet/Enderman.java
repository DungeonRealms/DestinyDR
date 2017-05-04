package net.dungeonrealms.game.world.entity.type.pet;

import com.google.common.base.Optional;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.45F);
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

    public void teleport() {
        Player owner = Bukkit.getPlayer(ownerUUID);
        if(owner == null) return;
        double xCoord = getRandomDouble(owner.getLocation().getX() - 2,owner.getLocation().getX() + 2);
        double zCoord = getRandomDouble(owner.getLocation().getZ() - 2,owner.getLocation().getZ() + 2);

        Location currentLocation = new Location(this.getBukkitEntity().getWorld(),this.lastX, this.lastY, this.lastZ);
        Location newLocation = new Location(this.getBukkitEntity().getWorld(),xCoord, this.lastY, zCoord);
        if(currentLocation.distanceSquared(newLocation) < 2) return;
        this.teleport(xCoord, owner.getLocation().getY(), zCoord);
    }

    private boolean teleport(double d0, double d1, double d2) {
        boolean flag = this.k(d0, d1, d2);
        if(flag) {
            this.world.a((EntityHuman)null, this.lastX, this.lastY, this.lastZ, SoundEffects.ba, this.bA(), 1.0F, 1.0F);
            //this.a(SoundEffects.ba, 1.0F, 1.0F);
        }

        return flag;
    }

    public Enderman(World world) {
        super(world);
    }

    @Override
    protected void r() {

    }

    @Override
    public void n() {
        super.n();
        if(ticksLived % 50 == 0) teleport();
    }


    public double getRandomDouble(double min, double max) {
        double random = this.getRandom().nextDouble();
        double result = min + (random * (max - min));

        return result;
    }
}
