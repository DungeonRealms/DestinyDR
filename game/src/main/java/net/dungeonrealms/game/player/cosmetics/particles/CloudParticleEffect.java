package net.dungeonrealms.game.player.cosmetics.particles;

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
public class CloudParticleEffect extends SpecialParticleEffect {

    public CloudParticleEffect(Location toPlay) {
        super(toPlay);
    }

    public CloudParticleEffect(LivingEntity toPlay) {
        super(toPlay);
    }

    @Override
    public void tick() {
        Location toPlayEffect = getLocation();
        constructCloud(toPlayEffect);
    }

    @Override
    public long getTickRate() {
        return 1;
    }

    protected void constructCloud(Location toPlay) {
        ParticleAPI.spawnParticle(Particle.SMOKE_LARGE, toPlay, ThreadLocalRandom.current().nextFloat() * 5f, Utils.randInt(10), .3F);
    }


}
