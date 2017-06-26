package net.dungeonrealms.game.player.cosmetics.particles.impl;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticleEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 6/26/2017.
 */
public class CloudParticleEffect extends SpecialParticleEffect {

    ThreadLocalRandom random = ThreadLocalRandom.current();

    public CloudParticleEffect(Location toPlay) {
        super(toPlay);
    }

    public CloudParticleEffect(LivingEntity toPlay) {
        super(toPlay);
    }

    @Override
    public void tick() {
        if(!canTick()) return;
        Location toPlayEffect = getLocation().clone().add(0,1,0);
        constructCloud(toPlayEffect);
        if(random.nextInt(5) == 3)constructRain(toPlayEffect);

        lastTick = System.currentTimeMillis();
    }

    @Override
    public long getTickRate() {
        return 10;
    }

    protected void constructCloud(Location toPlay) {
        ParticleAPI.spawnParticle(Particle.REDSTONE, toPlay, Utils.randFloat(0, 0.5f),0f,Utils.randFloat(0, 0.5f), 5, 0.05F);
    }

    protected void constructRain(Location toPlay) {
        ParticleAPI.spawnParticle(Particle.DRIP_WATER, toPlay, Utils.randFloat(0, 0.5f),0f,Utils.randFloat(0, 0.5f), 5, .3F);
    }



}
