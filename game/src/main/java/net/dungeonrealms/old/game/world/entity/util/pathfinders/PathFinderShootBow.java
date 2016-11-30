package net.dungeonrealms.old.game.world.entity.util.pathfinders;

import net.dungeonrealms.old.game.world.entity.type.monster.type.ranged.RangedZombie;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.EnumHand;
import net.minecraft.server.v1_9_R2.ItemBow;
import net.minecraft.server.v1_9_R2.PathfinderGoal;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class PathFinderShootBow extends PathfinderGoal {
    private final RangedZombie a;
    private final double b;
    private int c;
    private final float d;
    private int e = -1;
    private int f;
    private boolean g;
    private boolean h;
    private int i = -1;

    public PathFinderShootBow(RangedZombie var1, double var2, int var4, float var5) {
        this.a = var1;
        this.b = var2;
        this.c = var4;
        this.d = var5 * var5;
        this.a(3);
    }

    public void b(int var1) {
        this.c = var1;
    }

    public boolean a() {
        return this.a.getGoalTarget() != null;
    }


    public boolean b() {
        return (this.a() || !this.a.getNavigation().n());
    }

    public void c() {
        super.c();
        this.a.a(true);
    }

    public void d() {
        super.c();
        this.a.a(false);
        this.f = 0;
        this.e = -1;
        this.a.cA();
    }

    public void e() {
        EntityLiving var1 = this.a.getGoalTarget();
        if (var1 != null) {
            double var2 = this.a.e(var1.locX, var1.getBoundingBox().b, var1.locZ);
            boolean var4 = this.a.getEntitySenses().a(var1);
            boolean var5 = this.f > 0;
            if (var4 != var5) {
                this.f = 0;
            }

            if (var4) {
                ++this.f;
            } else {
                --this.f;
            }

            if (var2 <= (double) this.d && this.f >= 20) {
                this.a.getNavigation().o();
                ++this.i;
            } else {
                this.a.getNavigation().a(var1, this.b);
                this.i = -1;
            }

            if (this.i >= 20) {
                if ((double) this.a.getRandom().nextFloat() < 0.3D) {
                    this.g = !this.g;
                }

                if ((double) this.a.getRandom().nextFloat() < 0.3D) {
                    this.h = !this.h;
                }

                this.i = 0;
            }

            if (this.i > -1) {
                if (var2 > (double) (this.d * 0.75F)) {
                    this.h = false;
                } else if (var2 < (double) (this.d * 0.25F)) {
                    this.h = true;
                }

                this.a.getControllerMove().a(this.h ? -0.5F : 0.5F, this.g ? 0.5F : -0.5F);
                this.a.a(var1, 30.0F, 30.0F);
            } else {
                this.a.getControllerLook().a(var1, 30.0F, 30.0F);
            }

            if (this.a.ct()) {
                if (!var4 && this.f < -60) {
                    this.a.cA();
                } else if (var4) {
                    int var6 = this.a.cy();
                    if (var6 >= 20) {
                        this.a.cA();
                        this.a.a(var1, ItemBow.b(var6));
                        this.e = this.c;
                    }
                }
            } else if (--this.e <= 0 && this.f >= -60) {
                this.a.c(EnumHand.MAIN_HAND);
            }

        }
    }
}
