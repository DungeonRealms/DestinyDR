package net.dungeonrealms.game.mechanic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.GameAPI;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ParticleAPI - Basic Particle Utilities
 * 
 * Redone May 20th, 2017.
 * @author Kneesnap
 */
public class ParticleAPI {

    @AllArgsConstructor @Getter
    public enum ParticleEffect {
        FIREWORKS_SPARK(Material.FIREWORK, "Fireworks"),
        TOWN_AURA(Material.SULPHUR, "Stubble"),
        CRIT(Material.NETHER_STAR, "Light Stars"),
        CRIT_MAGIC(Material.FIREWORK_CHARGE, "Dark Stars"),
        SPELL_WITCH(Material.CAULDRON_ITEM, "Magic"),
        NOTE(Material.NOTE_BLOCK, "Notes", 1000),
        PORTAL(Material.EYE_OF_ENDER, "Portal", 1000),
        ENCHANTMENT_TABLE(Material.ENCHANTMENT_TABLE, "Enchantment"),
        FLAME(Material.FIREBALL, "Flames", 1000),
        WATER_SPLASH(Material.WATER_BUCKET, "Splash"),
        REDSTONE(Material.CAKE, "Birthday", ChatColor.RED, 1000),
        SNOWBALL(Material.SNOW_BALL, "Snowball"),
        SMOKE_NORMAL(Material.SUGAR, "Thin Smoke", 1000),
        CLOUD(Material.BEACON, "Cloudy", 1150),
        VILLAGER_HAPPY(Material.SPIDER_EYE, "Poison", ChatColor.DARK_GREEN, 1000),
        SNOW_SHOVEL(Material.SNOW, "Snowfall"),
        HEART(Material.APPLE, "Hearts", 1000),
        GOLD_BLOCK(Material.GOLD_BLOCK, "Golden Curse", -1);

        private Material material;
        private String displayName;
        private ChatColor color;
        private int price;
        
        ParticleEffect(Material mat, String displayName) {
            this(mat, displayName, 650);
        }

        ParticleEffect(Material mat, String displayName, int price) {
            this(mat, displayName, ChatColor.WHITE, price);
        }

        /**
         * Is this particle effect enabled?
         * @return
         */
        public boolean isEnabled() {
        	return getPrice() > 0;
        }
        
        public ItemStack getSelectionItem() {
        	return new ItemStack(getMaterial());
        }
        
        public Particle getParticle() {
            return Particle.valueOf(name());
        }

        public int getId() {
            return ordinal();
        }

        public static ParticleEffect getById(int id) {
            for (ParticleEffect particleEffect : values())
                if (particleEffect.getId() == id)
                    return particleEffect;
            return null;
        }

        public static ParticleEffect getByName(String rawName) {
            for (ParticleEffect particleEffect : values())
                if (particleEffect.name().equalsIgnoreCase(rawName))
                    return particleEffect;
            return null;
        }
    }
    
    /**
     * Spawn the block step particles for a given block.
     * @param loc
     * @param mat
     */
    @SuppressWarnings("deprecation")
	public static void spawnBlockParticles(Location loc, Material mat) {
    	loc.getWorld().playEffect(loc, Effect.STEP_SOUND, mat.getId());
    }
    
    /**
     * Spawns a particle at the given location. Async Safe.
     * @param p
     * @param loc
     * @param count
     * @param offset
     * @param speed
     */
    public static void spawnParticle(Particle p, Location loc, int count, float offset, float speed) {
        spawnParticle(p, loc, offset, offset, offset, count, speed);
    }

    /**
     * Spawns a particle at the given location. Async Safe.
     * @param p
     * @param loc
     * @param count
     * @param speed
     */
    public static void spawnParticle(Particle p, Location loc, int count, float speed) {
        spawnParticle(p, loc, 0F, count, speed);
    }
    
    /**
     * Spawns a particle at the given location, with the random factor having a constant offset.
     * Async safe.
     * @param p
     * @param loc
     * @param randOffset
     * @param count
     * @param speed
     */
    public static void spawnParticle(Particle p, Location loc, float randOffset, int count, float speed) {
    	Random r = ThreadLocalRandom.current();
        spawnParticle(p, loc, r.nextFloat() + randOffset, r.nextFloat() + randOffset, r.nextFloat() + randOffset, count, speed);
    }

    /**
     * Spawns a particle at the given location. Async Safe.
     * @param p
     * @param loc
     * @param xOff
     * @param yOff
     * @param zOff
     * @param count
     * @param speed
     */
    public static void spawnParticle(Particle p, Location loc, double xOff, double yOff, double zOff, int count, float speed) {
        GameAPI.getNearbyPlayersAsync(loc, 30).forEach(pl -> pl.spawnParticle(p, loc, count, xOff, yOff, zOff, speed));
    }


    /**
     * Spawns a particle at the given location. Async Safe.
     * @param p
     * @param loc
     * @param xOff
     * @param yOff
     * @param zOff
     * @param count
     * @param speed
     */
    public static void spawnParticleWithData(Particle p, Location loc, double xOff, double yOff, double zOff, int count, float speed, int data) {
        GameAPI.getNearbyPlayersAsync(loc, 30).forEach(pl -> pl.spawnParticle(p, loc, count, speed, xOff, yOff, zOff, data));
    }
}
