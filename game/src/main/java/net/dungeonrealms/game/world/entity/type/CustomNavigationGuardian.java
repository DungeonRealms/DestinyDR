package net.dungeonrealms.game.world.entity.type;

import net.minecraft.server.v1_9_R2.*;

public class CustomNavigationGuardian  extends NavigationAbstract {
    public CustomNavigationGuardian(EntityInsentient var1, World var2) {
        super(var1, var2);
    }

    protected Pathfinder a() {
        return new Pathfinder(new PathfinderNormal());
    }

    protected boolean b() {
        return this.p();
    }

    protected Vec3D c() {
        return new Vec3D(this.a.locX, this.a.locY + (double)this.a.length * 0.5D, this.a.locZ);
    }

    protected void m() {
        Vec3D var1 = this.c();
        float var2 = this.a.width * this.a.width;
        byte var3 = 6;
        if(var1.distanceSquared(this.c.a(this.a, this.c.e())) < (double)var2) {
            this.c.a();
        }

        for(int var4 = Math.min(this.c.e() + var3, this.c.d() - 1); var4 > this.c.e(); --var4) {
            Vec3D var5 = this.c.a(this.a, var4);
            if(var5.distanceSquared(var1) <= 36.0D && this.a(var1, var5, 0, 0, 0)) {
                this.c.c(var4);
                break;
            }
        }

        this.a(var1);
    }

    protected void d() {
        super.d();
    }

    protected boolean a(Vec3D var1, Vec3D var2, int var3, int var4, int var5) {
        MovingObjectPosition var6 = this.b.rayTrace(var1, new Vec3D(var2.x, var2.y + (double)this.a.length * 0.5D, var2.z), false, true, false);
        return var6 == null || var6.type == MovingObjectPosition.EnumMovingObjectType.MISS;
    }

    public boolean b(BlockPosition var1) {
        return !this.b.getType(var1).b();
    }
}