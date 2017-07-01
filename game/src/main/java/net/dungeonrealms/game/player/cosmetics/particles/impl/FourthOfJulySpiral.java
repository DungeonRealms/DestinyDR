package net.dungeonrealms.game.player.cosmetics.particles.impl;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.cosmetics.particles.RedstoneParticleColor;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticleEffect;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticles;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 6/30/2017.
 */
public class FourthOfJulySpiral extends SpecialParticleEffect {



    private int lastDegree = 0;
    private double lastY = 0;
    private double height = 0;
    private int colorThreshhold = 0;
    private int startingDegree = 0;

    public FourthOfJulySpiral(Location toPlay, double height, int startingDegree) {
        super(toPlay);
        this.height = height;
        lastDegree = startingDegree;
        this.startingDegree = startingDegree;
    }

    public FourthOfJulySpiral(LivingEntity toPlay, double height, int startingDegree) {
        super(toPlay);
        this.height = height;
        lastDegree = startingDegree;
        this.startingDegree = startingDegree;
    }

    @Override
    public void tick() {
        //if(!canTick()) return;
        Location center = getLocation().clone();
        double radians = Math.toRadians(lastDegree);
        double x = Math.cos(radians);
        double z = Math.sin(radians);
        double y = lastY;
        center.add(x,y,z);
        RedstoneParticleColor color = getNextColor();
        ParticleAPI.spawnParticle(Particle.REDSTONE, center, color.getRed(), color.getGreen(),color.getBlue(), 0, 1F);
        lastDegree += 5;
        lastY += 0.05;
        colorThreshhold += 1;
        if(colorThreshhold >= 30) colorThreshhold = 0;
        if(lastY >= height) {
            lastY = 0;
            lastDegree = startingDegree;
        }

        lastTick = System.currentTimeMillis();
    }


    @Override
    public long getTickRate() {
        return 1;
    }

    private RedstoneParticleColor getNextColor(){
        if(colorThreshhold >= 20) return RedstoneParticleColor.DARK_BLUE;
        if(colorThreshhold >= 10) return RedstoneParticleColor.WHITE;
        return RedstoneParticleColor.DARK_RED;
    }

    @Override
    public SpecialParticles getParticleEnum() {
        return null;
    }

    @Override
    public boolean tickWhileMoving() {
        return false;
    }

}
