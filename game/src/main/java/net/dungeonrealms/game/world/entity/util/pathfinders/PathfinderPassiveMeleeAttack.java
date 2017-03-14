package net.dungeonrealms.game.world.entity.util.pathfinders;

import net.dungeonrealms.game.world.entity.type.monster.type.melee.PassiveDRChicken;
import net.minecraft.server.v1_9_R2.*;

public class PathfinderPassiveMeleeAttack extends PathfinderGoal {
    World a;
    protected PassiveDRChicken b;
    int c;
    double d;
    boolean e;
    PathEntity f;
    private int h;
    private double i;
    private double j;
    private double k;
    protected final int g = 20;

    public PathfinderPassiveMeleeAttack(PassiveDRChicken var1, double var2, boolean var4) {
        this.b = var1;
        this.a = var1.world;
        this.d = var2;
        this.e = var4;
        this.a(3);
    }

    //walk towards?
    public boolean a() {
        EntityLiving var1 = this.b.getGoalTarget();
        if (var1 == null) {
            return false;
        } else if (!var1.isAlive()) {
            return false;
        } else {
            this.f = this.b.getNavigation().a(var1);
            return this.f != null;
        }
    }

    public boolean b() {
        EntityLiving var1 = this.b.getGoalTarget();
        return var1 == null ? false : (!var1.isAlive() ? false : (!this.e ? !this.b.getNavigation().n() : (!this.b.f(new BlockPosition(var1)) ? false : !(var1 instanceof EntityHuman) || !((EntityHuman) var1).isSpectator() && !((EntityHuman) var1).l_())));
    }

    public void c() {
        this.b.getNavigation().a(this.f, this.d);
        this.h = 0;
    }

    public void d() {
        EntityLiving var1 = this.b.getGoalTarget();
        if (var1 instanceof EntityHuman && (((EntityHuman) var1).isSpectator() || ((EntityHuman) var1).l_())) {
            this.b.setGoalTarget((EntityLiving) null);
        }

        this.b.getNavigation().o();
    }

    public void e() {
        EntityLiving target = this.b.getGoalTarget();
        if (target != null && !(target instanceof EntityHuman)) {
            b.setGoalTarget(null);
            return;
        }

        this.b.getControllerLook().a(target, 30.0F, 30.0F);
        double distnace = this.b.e(target.locX, target.getBoundingBox().b, target.locZ);
        double bounding = this.a(target);
        --this.h;
        //                                                                                                                          Distance check, changed to .5 so it keeps trying to attack.
        if ((this.e || this.b.getEntitySenses().a(target)) && this.h <= 0 && (this.i == 0.0D && this.j == 0.0D && this.k == 0.0D || target.e(this.i, this.j, this.k) >= 0.5D || this.b.getRandom().nextFloat() < 0.05F)) {
            this.i = target.locX;
            this.j = target.getBoundingBox().b;
            this.k = target.locZ;
            this.h = 4 + this.b.getRandom().nextInt(7);
            if (distnace > 1024.0D) {
                this.h += 10;
            } else if (distnace > 256.0D) {
                this.h += 5;
            }

            if (!this.b.getNavigation().a(target, this.d)) {
                this.h += 15;
            }
        }

        this.c = Math.max(this.c - 1, 0);
        if (distnace <= bounding && this.c <= 0) {
            this.c = 10;
            //Swing item
            this.b.a(EnumHand.MAIN_HAND);
            //Damage entity?, Doesnt seem to work?
            this.b.B(target);

            //Manually damage to target with our API.
            EntityHuman human = (EntityHuman) target;
            human.getBukkitEntity().damage(20, b.getBukkitEntity());
        }

    }

    protected double a(EntityLiving var1) {
        return (double) (this.b.width * 2.0F * this.b.width * 2.0F + var1.width);
    }
}
