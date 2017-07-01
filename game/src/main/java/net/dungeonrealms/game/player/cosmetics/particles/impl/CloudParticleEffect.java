package net.dungeonrealms.game.player.cosmetics.particles.impl;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticleEffect;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticles;
import net.minecraft.server.v1_9_R2.EnumParticle;
import net.minecraft.server.v1_9_R2.PacketPlayOutWorldParticles;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 6/26/2017.
 */
public class CloudParticleEffect extends SpecialParticleEffect {

    private static ThreadLocalRandom random = ThreadLocalRandom.current();

    public CloudParticleEffect(Location toPlay) {
        super(toPlay);
    }

    public CloudParticleEffect(LivingEntity toPlay) {
        super(toPlay);
    }

    @Override
    public void tick() {
        if(!canTick()) return;
        Location toPlayEffect = getLocation().clone().add(0,2.5,0);
        constructCloud(toPlayEffect);
        constructRain(toPlayEffect);
        if(random.nextInt(10) == 5) constructLightningBolt(getRandomBoltLocation(toPlayEffect));

        lastTick = System.currentTimeMillis();
    }

    private Location getRandomBoltLocation(Location baseLocation) {
        return baseLocation.clone().add(Utils.randFloat(-0.3f, 0.3f),0,Utils.randFloat(-0.3f, 0.3f));
    }


    @Override
    public long getTickRate() {
        return 10;
    }

    protected void constructCloud(Location toPlay) {
        ParticleAPI.spawnParticle(Particle.CLOUD, toPlay, Utils.randFloat(0, 0.5f),0.0f,Utils.randFloat(0, 0.5f), 10, 0F);
    }

    protected void constructLightningBolt(Location toStart) {
        boolean isZBolt = random.nextBoolean();
        boolean isBackwards = random.nextBoolean();
        for(int yIndex = 0; yIndex < lightningBolt.length; yIndex++) {
            for(int xIndex = 0; xIndex < lightningBolt[0].length; xIndex++) {
                if(lightningBolt[yIndex][xIndex] == 0) continue;
                double x = isBackwards ? toStart.getX() - (xIndex / 5.0) : toStart.getX() + (xIndex / 5.0);
                double y = toStart.getY() - (yIndex / 10.0);
                double z = toStart.getZ();
                if(isZBolt) {
                    x = toStart.getX();
                    if(isBackwards) z -= (xIndex / 5.0);
                    else z += (xIndex / 5.0);
                }
                //ParticleAPI.spawnParticle(Particle.FLAME, new Location(toStart.getWorld(), x,y,z), 0,0f,0, 1, 0F);
                ParticleAPI.spawnParticle(Particle.REDSTONE, new Location(toStart.getWorld(), x,y,z), 1,0.95,0f, 0, 1F);
            }
        }
    }

    @Override
    public boolean tickWhileMoving() {
        return false;
    }

    @Override
    public SpecialParticles getParticleEnum() {
        return SpecialParticles.CLOUD;
    }

    protected void constructRain(Location toPlay) {
        ParticleAPI.spawnParticle(Particle.DRIP_WATER, toPlay, Utils.randFloat(0, 0.3f),0f,Utils.randFloat(0, 0.3f), 5, .3F);
    }

    private static final int[][] lightningBolt = {
            {0,0,0,1,0},
            {0,0,0,1,0},
            {0,0,0,1,0},
            {0,0,0,1,0},
            {0,0,0,1,0},
            {0,0,0,1,0},
            {0,0,0,1,0},
            {0,0,0,1,0},
            {0,0,0,1,0},
            {0,0,0,1,0},
            {0,0,1,0,0},
            {0,0,1,0,0},
            {0,0,1,0,0},
            {0,0,1,0,0},
            {0,0,1,0,0},
            {0,0,1,0,0},
            {0,0,1,0,0},
            {0,0,1,0,0},
            {0,0,1,0,0},
            {0,0,1,0,0},
            {0,0,1,0,0},
            {0,0,1,0,0},
            {0,0,1,0,0},
            {0,1,0,0,0},
            {0,1,0,0,0},
            {0,1,0,0,0},
            {0,1,0,0,0},
    };



}
