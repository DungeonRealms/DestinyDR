package net.dungeonrealms.game.player.cosmetics.particles;

import lombok.Getter;
import lombok.SneakyThrows;
import net.dungeonrealms.game.player.cosmetics.particles.impl.CloudParticleEffect;
import net.dungeonrealms.game.player.cosmetics.particles.impl.FourthOfJulyAuraEffect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * Created by Rar349 on 6/30/2017.
 */
@Getter
public enum SpecialParticles {

    CLOUD("cloud", CloudParticleEffect.class),
    FOURTH_AURA("fourthOfJulyAura", FourthOfJulyAuraEffect.class);


    String internalName;
    Class<? extends SpecialParticleEffect> theClass;
    SpecialParticles(String internalName, Class<? extends SpecialParticleEffect> theClass) {
        this.internalName = internalName;
        this.theClass = theClass;
    }

    public static SpecialParticles fromInternal(String internalName) {
        for(SpecialParticles particle : SpecialParticles.values()) {
            if(!particle.getInternalName().equals(internalName)) continue;
            return particle;
        }

        return null;
    }

    @SneakyThrows
    public static SpecialParticleEffect constrauctEffectFromName(String name, LivingEntity toFollow) {
        for(SpecialParticles particle : SpecialParticles.values()) {
            if(!particle.getInternalName().equals(name)) continue;
            return particle.theClass.getConstructor(LivingEntity.class).newInstance(toFollow);
        }
        return null;
    }

    @SneakyThrows
    public static SpecialParticleEffect constrauctEffectFromName(String name, Location toPlay) {
        for(SpecialParticles particle : SpecialParticles.values()) {
            if(!particle.getInternalName().equals(name)) continue;
            return particle.theClass.getConstructor(Location.class).newInstance(toPlay);
        }
        return null;
    }

}
