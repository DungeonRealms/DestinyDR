package net.dungeonrealms.game.player.cosmetics.particles.impl;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticleEffect;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticles;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rar349 on 6/27/2017.
 */
public class FourthOfJulyAuraEffect extends SpecialParticleEffect {

    private List<FourthOfJulySpiral> spirals = new ArrayList<>();



    public FourthOfJulyAuraEffect(Location toPlay) {
        super(toPlay);
        for(int k = 0; k < 4; k++) {
            spirals.add(new FourthOfJulySpiral(toPlay,2, k * 90));
        }
    }

    public FourthOfJulyAuraEffect(LivingEntity toPlay) {
        super(toPlay);
        for(int k = 0; k < 4; k++) {
            spirals.add(new FourthOfJulySpiral(toPlay,2, k * 90));
        }
    }

    @Override
    public void tick() {
        for(FourthOfJulySpiral spiral : spirals) {
            spiral.tick();
        }
    }

    @Override
    public boolean tickWhileMoving() {
        return false;
    }

    @Override
    public SpecialParticles getParticleEnum() {
        return SpecialParticles.FOURTH_AURA;
    }

    @Override
    public long getTickRate() {
        return 1;
    }
}
