package net.dungeonrealms.game.world.entity.util.pathfinders;

import net.minecraft.server.v1_9_R2.*;

public class PathfinderGoalMeleeAttackWell extends PathfinderGoal {
    World a;
    protected EntityCreature entity;
    int c;
    double d;
    boolean e;
    PathEntity f;
    private int h;
    private double lastTargetX;
    private double lastTargetY;
    private double lastTargetZ;
    protected final int g = 20;

    public PathfinderGoalMeleeAttackWell(final EntityCreature var1, final double var2, final boolean var4) {
        this.entity = var1;
        this.a = var1.world;
        this.d = var2;
        this.e = var4;
        this.a(3);
    }

    public boolean a() {
        final EntityLiving var1 = this.entity.getGoalTarget();
        if (var1 == null) {
            return false;
        }
        if (!var1.isAlive()) {
            return false;
        }
        this.f = this.entity.getNavigation().a((Entity) var1);
        return this.f != null;
    }

    public boolean b() {
        final EntityLiving var1 = this.entity.getGoalTarget();
        if (var1 != null && var1.isAlive()) {
            if (!this.e) {
                final boolean retr = !this.entity.getNavigation().n();
                if (!retr) {
                    final double var2 = this.entity.e(var1.locX, var1.getBoundingBox().b, var1.locZ);
                    final double var3 = this.a(var1);
                    if (var2 <= var3) {
                        return true;
                    }
                }
                return retr;
            }
            if (!this.entity.f(new BlockPosition((Entity) var1))) {
                return false;
            }
            if (!(var1 instanceof EntityHuman) || (!((EntityHuman) var1).isSpectator() && !((EntityHuman) var1).l_())) {
                return true;
            }
        }
        return false;
    }

    public void c() {
        this.entity.getNavigation().a(this.f, this.d);
        this.h = 0;
    }

    public void d() {
        final EntityLiving var1 = this.entity.getGoalTarget();
        if (var1 instanceof EntityHuman && (((EntityHuman) var1).isSpectator() || ((EntityHuman) var1).l_())) {
            this.entity.setGoalTarget((EntityLiving) null);
        }
        this.entity.getNavigation().o();
    }

    public void e() {
        final EntityLiving var1 = this.entity.getGoalTarget();
        this.entity.getControllerLook().a((Entity) var1, 30.0f, 30.0f);
        final double var2 = this.entity.e(var1.locX, var1.getBoundingBox().b, var1.locZ);
        final double var3 = this.a(var1);
        --this.h;
        if ((this.e || this.entity.getEntitySenses().a((Entity) var1)) && this.h <= 0 && ((this.lastTargetX == 0.0 && this.lastTargetY == 0.0 && this.lastTargetZ == 0.0) || var1.e(this.lastTargetX, this.lastTargetY, this.lastTargetZ) >= 1.0 || this.entity.getRandom().nextFloat() < 0.05f)) {
            this.lastTargetX = var1.locX;
            this.lastTargetY = var1.getBoundingBox().b;
            this.lastTargetZ = var1.locZ;
            this.h = 4 + this.entity.getRandom().nextInt(7);
            if (var2 > 1024.0) {
                this.h += 10;
            } else if (var2 > 256.0) {
                this.h += 5;
            }
            if (!this.entity.getNavigation().a((Entity) var1, this.d)) {
                this.h += 15;
            }
        }
        this.c = Math.max(this.c - 1, 0);
        if (var2 <= var3 && this.c <= 0) {
            this.c = 20;
            this.entity.a(EnumHand.MAIN_HAND);
            this.entity.B((Entity) var1);
        }
    }

    protected double a(final EntityLiving var1) {
        return this.entity.width * 2.0f * this.entity.width * 2.0f + var1.width;
    }

    public int hashCode() {
        return this.entity.hashCode() + this.a.hashCode();
    }
}
