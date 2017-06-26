package net.dungeonrealms.game.player.cosmetics.particles;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * Created by Rar349 on 6/26/2017.
 */
public abstract class SpecialParticleEffect {

    private final Location stationaryLocation;
    private final LivingEntity entityToPlay;
    protected long lastTick = 0L;

    public SpecialParticleEffect(Location toPlay) {
        this.stationaryLocation = toPlay;
        this.entityToPlay = null;
    }

    public SpecialParticleEffect(LivingEntity toPlay) {
        this.entityToPlay = toPlay;
        this.stationaryLocation = null;
    }

    public boolean canTick() {
        return System.currentTimeMillis() - lastTick >= getTickRate();
    }

    public abstract void tick();

    public abstract long getTickRate();

    public Location getLocation() {
        if(entityToPlay != null) return entityToPlay.getEyeLocation().clone();
        return stationaryLocation.clone();
    }

}
