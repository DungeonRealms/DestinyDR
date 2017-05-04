package net.dungeonrealms.game.world.entity.type.pet;

import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.CustomNavigationGuardian;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by Rar349 on 4/30/2017.
 */
public class Guardian extends EntityGuardian {

    private String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;

    private int ownerID;
    public Guardian(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
        super(world);
        this.mobName = mobName;
        this.ownerUUID = ownerUUID;
        this.entityType = entityType;
        this.getBukkitEntity().setCustomNameVisible(true);
        this.getBukkitEntity().setCustomName(mobName);
        this.canPickUpLoot = false;
        this.persistent = true;
        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.3F);
    }



    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        return false;
    }

    @Override
    public void setGoalTarget(@Nullable EntityLiving entityliving) {

    }


    public Guardian(World world) {
        super(world);
    }

    @Override
    protected void r() {

    }

    @Override
    public boolean isInWater() {
        return true;
    }

    @Override
    public void move(double d0, double d1, double d2) {
        super.move(d0, d1, d2);
    }

    protected NavigationAbstract b(World var1) {
        return new CustomNavigationGuardian(this, var1);
    }

    @Override
    public boolean cp() {
        return true;
    }

    @Override
    public void n() {
        if(this.onGround) {
            this.motY += 0.5D;
            this.onGround = false;
            this.impulse = true;
        }
        super.n();
    }
}
