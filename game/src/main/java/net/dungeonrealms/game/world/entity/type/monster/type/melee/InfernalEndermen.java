package net.dungeonrealms.game.world.entity.type.monster.type.melee;

import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.SoundEffects;
import net.minecraft.server.v1_9_R2.Vec3D;
import net.minecraft.server.v1_9_R2.World;

public class InfernalEndermen extends MeleeEnderman {

    public InfernalEndermen(World world) {
        super(world);
        this.persistent = true;
    }

    @Override
    protected boolean a(Entity entity) {
        Vec3D vec3d = new Vec3D(this.locX - entity.locX, this.getBoundingBox().b + (double) (this.length / 2.0F) - entity.locY + (double) entity.getHeadHeight(), this.locZ - entity.locZ);
        vec3d = vec3d.a();
        double d0 = 2.0D;
        double d1 = this.locX + (this.random.nextDouble() - 0.5D) * 1.0D - vec3d.x * d0;
        double d2 = this.locY + (double) (this.random.nextInt(5) - 5) - vec3d.y * d0;
        double d3 = this.locZ + (this.random.nextDouble() - 0.5D) * 1.0D - vec3d.z * d0;
        return this.l(d1, d2, d3);
    }

    @Override
    protected void M() {
        return;
    }

    @Override
    protected boolean db() {
        return false;
    }

    @Override
    public boolean k(double d0, double d1, double d2) {
        return false;
    }

    private boolean l(double d0, double d1, double d2) {
        boolean flag = this.k(d0, d1, d2);
        if (flag) {
            this.world.a(null, this.lastX, this.lastY, this.lastZ, SoundEffects.ba, this.bA(), 1.0F, 1.0F);
            this.a(SoundEffects.ba, 1.0F, 1.0F);
        }

        return flag;
    }

}
