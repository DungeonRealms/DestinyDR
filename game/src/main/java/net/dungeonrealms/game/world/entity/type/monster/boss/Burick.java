package net.dungeonrealms.game.world.entity.type.monster.boss;

import io.netty.util.internal.ConcurrentSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.MeleeWitherSkeleton;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Burick The Fanatic Dungeon Boss.
 * <p>
 * Redone on April 29th, 2017.
 *
 * @author Kneesnap
 */
public class Burick extends MeleeWitherSkeleton implements DungeonBoss {

    private boolean firstHeal = false;
    private boolean isEnraged = false;
    private Set<Entity> spawnedMobs = new ConcurrentSet<>();
    private boolean canAddsRespawn = true;

    public Burick(World world) {
        super(world);
        this.fireProof = true;
        this.collides = true;

        getBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, true, Color.RED));
    }

    public void startEnragedMode() {
        isEnraged = true;
        say("Pain. Sufferring. Agony. These are the emotions you will be feeling for the rest of eternity!");

        getDungeon().announce(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "Burick The Fanatic " + ChatColor.GOLD + "has become ENRAGED"
                + ChatColor.GOLD + " 2X DMG, +80% ARMOR, 2x SPEED, KNOCKBACK IMMUNITY!");
        playSound(Sound.ENTITY_ENDERMEN_DEATH, 0.8F, 0.5F);
        playSound(Sound.ENTITY_ENDERMEN_DEATH, 1.2F, 0.2F);
        playSound(Sound.ENTITY_ENDERMEN_DEATH, 0.8F, 1.2F);
        DamageAPI.setDamageBonus(getBukkit(), 100);
        DamageAPI.setArmorBonus(getBukkit(), 30);
        getAttributeInstance(GenericAttributes.c).setValue(1.00d);
        getBukkit().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, true));
    }

    @Override
    public void o() {
        super.o();
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(40);
    }

    double maxMove = 4;
    double currentMove = 0;

    @Override
    public void n() {
        if (ticksLived % 4 == 0) {
            for (Entity entity : spawnedMobs) {
                if (entity.isDead())
                    spawnedMobs.remove(entity);
            }
            // He's ready to fight again.
            if (spawnedMobs.isEmpty() && !isVulnerable()) {
                if (!canAddsRespawn)
                    say("Face me, pathetic creatures!");

                getBukkit().removePotionEffect(PotionEffectType.INVISIBILITY);
                setVulnerable(true);
                this.currentMove = 0;
            }
        }

        //Dont even tick his methods so he floats..
        if (!this.isVulnerable() && this.currentMove < maxMove) {
            //Particles?
            ParticleAPI.spawnParticle(Particle.SMOKE_NORMAL, getBukkitEntity().getLocation().add(0, .15, 0), 0, 0, 0, 10, .15F);
            this.currentMove += .1D;
            this.motY = .1D;
        } else if (!this.isVulnerable() && this.motY <= 0) {
            ParticleAPI.spawnParticle(Particle.PORTAL, getBukkitEntity().getLocation().add(0, .75, 0), ThreadLocalRandom.current().nextFloat() / 2,
                    ThreadLocalRandom.current().nextFloat() / 2, ThreadLocalRandom.current().nextFloat() / 2, 50, 1);
            return;
        }

        super.n();
    }

    @Override
    public void onBossAttacked(Player attacker) {

        if (getPercentHP() <= 0.5F && canAddsRespawn) {
            spawnWave();
            ParticleAPI.spawnParticle(Particle.SPELL, getBukkitEntity().getLocation(), 100, 1F);
            getBukkit().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
            this.motY += .1D;
            this.motX = this.motZ = 0;
            setVulnerable(false);
            this.currentMove = 0;
            say("To me, my undead brethren! Rip these Andalucians to pieces!");
            getDungeon().announce(ChatColor.GRAY + "Burick uses the energy of his minions to create a forcefield around himself -- kill the minions!");
            playSound(Sound.ENTITY_ENDERMEN_SCREAM, 1F, 0.5F);
            playSound(Sound.ENTITY_WITHER_SPAWN, 1F, 1.1F);
            canAddsRespawn = false;
        } else if (getPercentHP() <= 0.1F) {
            if (firstHeal) {
                if (!isEnraged)
                    startEnragedMode();
            } else {
                HealthHandler.initHP(getBukkit(), getMaxHP());
                canAddsRespawn = true;
                firstHeal = true;
                say("Let the powers of Maltai channel into me and give me strength!");
                playSound(Sound.ENTITY_ENDERMEN_DEATH, 1F, 0.5F);
            }
        }
    }

    @Override
    public BossType getBossType() {
        return BossType.Burick;
    }

    private void spawnWave() {
        MinionType type = MinionType.getRandom();
        for (int i = 0; i < type.getGroupSize(); i++)
            spawnedMobs.add(spawnMinion(type.getMonster(), type.getName(), type.getTier()));
    }

    @AllArgsConstructor
    private enum MinionType {
        PROTECTOR(EnumMonster.Monk, 2, 4),
        ACOLYTE(EnumMonster.Acolyte, 3, 4),
        SACRIFICE(EnumMonster.Skeleton, 2, 6);

        @Getter
        private EnumMonster monster;
        @Getter
        private int tier;
        @Getter
        private int groupSize;

        public String getName() {
            return "Burick's " + Utils.capitalize(name());
        }

        public static MinionType getRandom() {
            return values()[ThreadLocalRandom.current().nextInt(values().length)];
        }
    }
}