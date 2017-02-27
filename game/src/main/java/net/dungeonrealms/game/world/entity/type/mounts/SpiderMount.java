package net.dungeonrealms.game.world.entity.type.mounts;

import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.UUID;

public class SpiderMount extends EntitySpider {

    public UUID owner;

    public EnumMounts mount;

    public SpiderMount(World world, UUID owner, EnumMounts mount) {
        super(world);

        this.owner = owner;
        this.mount = mount;
        clearGoalSelectors();

        this.goalSelector.a(0, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 20F));

        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(mount.getMountData().getSpeed());
    }

    //Disable wall climbing hopefully?
    @Override
    public boolean n_() {
        return false;
    }

    //Ambient sound.
    @Override
    protected SoundEffect G() {
        return SoundEffects.bK;
    }

    @Override
    public void n() {
        for (int i = 0; i < 2; ++i) {
            this.world.addParticle(EnumParticle.PORTAL, this.locX + (this.random.nextDouble() - 0.5D) * (double) this.width, this.locY + this.random.nextDouble() * (double) this.length - 0.25D, this.locZ + (this.random.nextDouble() - 0.5D) * (double) this.width, (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D, new int[0]);
        }
        super.n();
    }

    private int floatTicks = 0;

    private long floatCooldown = -1;

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
//            die();
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

        if (jump != null) {
            if ((this.onGround || floatTicks < 18)) {
                jump.setAccessible(true);
                try {
                    if (jump.getBoolean(entityliving)) {
                        double jumpHeight = 0.5D;//Here you can set the jumpHeight

                        if (!this.onGround || floatTicks < 18) {
                            floatTicks++;
                            this.motY = .185D;
                            this.floatCooldown = System.currentTimeMillis() + 2000;
                            ParticleAPI.sendParticleToEntityLocation(ParticleAPI.ParticleEffect.SMALL_SMOKE, getBukkitEntity(), 0.0F, 0.0F, 0.0F, 0.01F, 3);
//                            this.world.addParticle(EnumParticle.SMOKE_NORMAL, this.locX, this.getBoundingBox().b, this.locY, 0.0D, 0.0D, 0.0D, new int[0]);
                        } else {
                            this.motY = jumpHeight;    // Used all the time in NMS for entity jumping
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.floatCooldown != -1) {
            if (this.floatCooldown < System.currentTimeMillis()) {
                floatCooldown = -1;
                this.floatTicks = 0;
            }
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

    //    m
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
