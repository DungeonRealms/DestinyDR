package net.dungeonrealms.game.player.cosmetics.particles.impl;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticleEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 6/26/2017.
 */
public class CapeParticleEffect extends SpecialParticleEffect {

    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    public CapeParticleEffect(Location toPlay) {
        super(toPlay);
    }

    public CapeParticleEffect(LivingEntity toPlay) {
        super(toPlay);
    }

    @Override
    public void tick() {
        if(!canTick()) return;
        Location toPlayEffect = getLocation().clone().add(0,1,0);
        constructWing(toPlayEffect);

        lastTick = System.currentTimeMillis();
    }



    @Override
    public long getTickRate() {
        return 10;
    }

    protected void constructWing(Location toPlay) {
        ParticleAPI.spawnParticle(Particle.CLOUD, toPlay, Utils.randFloat(0, 0.5f),0.0f,Utils.randFloat(0, 0.5f), 10, 0F);
    }



    private static final double[][] leftWing = {
            {0,0,0},
            {0,0,0},
            {0,0,0},
            {0,0,0},
            {0,0,0},
            {0,0,0},
            {0,0,0},
    };



}
