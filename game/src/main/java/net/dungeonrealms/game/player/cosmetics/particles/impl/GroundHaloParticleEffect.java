package net.dungeonrealms.game.player.cosmetics.particles.impl;

import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticleEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

/**
 * Created by Rar349 on 6/27/2017.
 */
public class GroundHaloParticleEffect extends SpecialParticleEffect {

    private int lastDegree = 0;

    public GroundHaloParticleEffect(Location toPlay) {
        super(toPlay);
    }

    public GroundHaloParticleEffect(LivingEntity toPlay) {
        super(toPlay);
    }

    @Override
    public void tick() {
        if(!canTick()) return;
        Location center = getLocation().clone();
        double radians = Math.toRadians(lastDegree);
        double x = Math.cos(radians);
        double z = Math.sin(radians);
        center.add(x,0,z);
        //location.getWorld().playEffect(location, Effect.FLAME, 1);
        ParticleAPI.spawnParticle(Particle.FLAME, center,0.0,0.0,0.0, 1, 0F);
        lastDegree += 5;
        if(lastDegree >= 360) lastDegree = 0;
    }


    @Override
    public long getTickRate() {
        return 1;
    }
}
