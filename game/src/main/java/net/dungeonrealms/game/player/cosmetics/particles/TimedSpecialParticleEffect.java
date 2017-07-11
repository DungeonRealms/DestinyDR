package net.dungeonrealms.game.player.cosmetics.particles;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Rar349 on 7/6/2017.
 */
public abstract class TimedSpecialParticleEffect extends SpecialParticleEffect {

    public static final CopyOnWriteArrayList<TimedSpecialParticleEffect> timedEffects = new CopyOnWriteArrayList<>();

    protected boolean isDead = false;
    private int ticksToDie;
    private final boolean isEntityEffect;

    public TimedSpecialParticleEffect(Location toPlay) {
        super(toPlay);
        this.isEntityEffect = false;
        if(!isSpecialTracked()) timedEffects.add(this);
    }

    public TimedSpecialParticleEffect(LivingEntity toPlay) {
        super(toPlay);
        this.isEntityEffect = true;
        if(!isSpecialTracked()) timedEffects.add(this);
    }

    @Override
    public void tick() {
        if(isDead) return;
        if(shouldDie()) {
            die();
            return;
        }

        if(!canTick()) return;
        ticksToDie++;
    }

    protected boolean shouldDie() {
        if(isEntityEffect) return getEntity() == null || getEntity().isDead() || ticksToDie >= getTicksToDie();
        return ticksToDie >= getTicksToDie();
    }

    protected abstract int getTicksToDie();

    protected void die() {
        if(!isSpecialTracked())timedEffects.remove(this);
        isDead = true;
    }

    protected abstract boolean isSpecialTracked();

    public static void tickTimedEffects() {
        for(TimedSpecialParticleEffect effect : timedEffects) {
            effect.tick();
        }
    }
}
