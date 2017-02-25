package net.dungeonrealms.game.world.entity.type.mounts;

import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.UUID;

public class SlimeMount extends EntitySlime {

    private EnumMounts mount;

    private UUID owner;

    public SlimeMount(World world, UUID owner, EnumMounts mount) {
        super(world);

        this.owner = owner;
        this.mount = mount;

        clearGoalSelectors();

        this.goalSelector.a(0, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 20F));

        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(mount.getMountData().getSpeed());

    }


    private int mountJumpTicks = 0;

    @Override
    public void g(float sideMotion, float forwardMotion) {
        if (!this.isVehicle() || passengers.size() == 0) {
            die();
            EntityMechanics.PLAYER_MOUNTS.remove(this.owner);
            Bukkit.getLogger().info("Entity dead no passengers..");
            return;
        }

        Entity entity = this.passengers.get(0);
        if (entity == null || !(entity instanceof EntityHuman)) {
            Bukkit.getLogger().info("Entity dead no passengers of human..");
            return;
        }

        EntityHuman entityliving = (EntityHuman) entity;

        this.lastYaw = this.yaw = entityliving.yaw;
        this.pitch = entityliving.pitch * 0.5F;
        this.setYawPitch(this.yaw, this.pitch);
        this.aP = this.aN = this.yaw;
        sideMotion = entityliving.be * 0.5F;
        forwardMotion = entityliving.bf;
        if (forwardMotion <= 0.0F) {
            forwardMotion *= 0.25F;
        }

        Field jump = null; //Jumping
        try {
            jump = EntityLiving.class.getDeclaredField("bd");
        } catch (NoSuchFieldException | SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        boolean jumped = false;
        if (jump != null && this.onGround) {    // Wouldn't want it jumping while on the ground would we?
            jump.setAccessible(true);
            try {
                if (jump.getBoolean(entityliving)) {
                    double jumpHeight = 0.5D;//Here you can set the jumpHeight
                    this.motY = jumpHeight;    // Used all the time in NMS for entity jumping
                    jumped = true;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


        if (!jumped && mountJumpTicks >= 24 && this.onGround) {
            this.motY = .6D;
            mountJumpTicks = 0;
            if (random.nextBoolean())
                this.world.getWorld().playSound(getBukkitEntity().getLocation(), Sound.BLOCK_SLIME_PLACE, 0.1F, 1);
        } else {
            mountJumpTicks++;
        }


        this.P = 1;
        this.aR = this.cl() * 0.1F;
        if (!this.world.isClientSide) {
            this.l((float) this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue());
            super.g(sideMotion, forwardMotion);
        }

        this.aF = this.aG;
        double d0 = this.locX - this.lastX;
        double d1 = this.locZ - this.lastZ;
        float f4 = MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;
        if (f4 > 1.0F) {
            f4 = 1.0F;
        }

        this.aG += (f4 - this.aG) * 0.4F;
        this.aH += this.aG;

    }


    private void clearGoalSelectors() {
        try {
            Field a = PathfinderGoalSelector.class.getDeclaredField("b");
            Field b = PathfinderGoalSelector.class.getDeclaredField("c");
            a.setAccessible(true);
            b.setAccessible(true);
            ((LinkedHashSet) a.get(this.goalSelector)).clear();
            ((LinkedHashSet) b.get(this.goalSelector)).clear();

            ((LinkedHashSet) a.get(this.targetSelector)).clear();
            ((LinkedHashSet) b.get(this.targetSelector)).clear();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
