package net.dungeonrealms.game.donate;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.database.type.EnumOperators;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.world.entities.types.pets.Creeper;
import net.minecraft.server.v1_9_R2.Entity;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Kieran on 10/1/2015.
 */
public class DonationEffects implements GenericMechanic {

    private static DonationEffects instance = null;

    public static DonationEffects getInstance() {
        if (instance == null) {
            instance = new DonationEffects();
        }
        return instance;
    }

    //CLOSED BETA PAYERS = RED_DUST
    //HALLOWEEN PLAYERS = SMALL_SMOKE
    //CHRISTMAS PLAYERS = SNOW_SHOVEL

    public HashMap<Player, ParticleAPI.ParticleEffect> PLAYER_PARTICLE_EFFECTS = new HashMap<>();
    public HashMap<Entity, ParticleAPI.ParticleEffect> ENTITY_PARTICLE_EFFECTS = new HashMap<>();
    public ConcurrentHashMap<Location, Material> PLAYER_GOLD_BLOCK_TRAIL_INFO = new ConcurrentHashMap<>();
    @Getter
    public Set<Creeper> fireWorkCreepers = new CopyOnWriteArraySet<>();
    public List<Player> PLAYER_GOLD_BLOCK_TRAILS = new ArrayList<>();
    @Getter
    @Setter
    public boolean lootBuffActive = false;
    private static Random random = new Random();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::spawnPlayerParticleEffects, 40L, 2L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::spawnEntityParticleEffects, 40L, 2L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::removeGoldBlockTrails, 40L, 4L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::handleCreeperFireworks, 40L, 100L);
    }

    @Override
    public void stopInvocation() {

    }

    private void handleCreeperFireworks() {
        if (fireWorkCreepers.isEmpty()) return;
        FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.BLUE, Color.RED, Color.WHITE).withFade(Color.BLUE, Color.RED, Color.WHITE).with(FireworkEffect.Type.STAR).trail(true).build();
        for (Creeper creeper : fireWorkCreepers) {
            if (!creeper.isAlive()) {
                fireWorkCreepers.remove(creeper);
                continue;
            }
            Firework fw = (Firework) creeper.getBukkitEntity().getWorld().spawnEntity(creeper.getBukkitEntity().getLocation(), EntityType.FIREWORK);
            FireworkMeta fwm = fw.getFireworkMeta();
            fwm.addEffect(effect);
            fwm.setPower(1); // 0.5 seconds
            fw.setFireworkMeta(fwm);
        }
    }

    public void spawnPlayerParticleEffects(Location location) {
        if (PLAYER_PARTICLE_EFFECTS.isEmpty()) return;
        for (Player player : PLAYER_PARTICLE_EFFECTS.keySet()) {
            if (!player.isOnline()) {
                PLAYER_PARTICLE_EFFECTS.remove(player);
                continue;
            }
            float moveSpeed = 0.02F;
            ParticleAPI.ParticleEffect particleEffect = PLAYER_PARTICLE_EFFECTS.get(player);
            if (particleEffect == ParticleAPI.ParticleEffect.RED_DUST || particleEffect == ParticleAPI.ParticleEffect.NOTE) {
                moveSpeed = -1F;
            }
            try {
                ParticleAPI.sendParticleToLocation(particleEffect, location.clone().add(0, 0.22, 0), (random.nextFloat()) - 0.4F, (random.nextFloat()) - 0.5F, (random.nextFloat()) - 0.5F, moveSpeed, 6);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.log.warning("[Donations] Could not spawn donation particle " + particleEffect.name() + " for player " + player.getName());
            }
        }
    }

    private void spawnPlayerParticleEffects() {
        if (PLAYER_PARTICLE_EFFECTS.isEmpty()) return;
        for (Player player : PLAYER_PARTICLE_EFFECTS.keySet()) {
            if (!player.isOnline()) {
                PLAYER_PARTICLE_EFFECTS.remove(player);
                continue;
            }
            float moveSpeed = 0.02F;
            ParticleAPI.ParticleEffect particleEffect = PLAYER_PARTICLE_EFFECTS.get(player);
            if (particleEffect == ParticleAPI.ParticleEffect.RED_DUST || particleEffect == ParticleAPI.ParticleEffect.NOTE) {
                moveSpeed = -1F;
            }
            try {
                ParticleAPI.sendParticleToLocation(particleEffect, player.getLocation().add(0, 0.22, 0), (random.nextFloat()) - 0.4F, (random.nextFloat()) - 0.5F, (random.nextFloat()) - 0.5F, moveSpeed, 6);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.log.warning("[Donations] Could not spawn donation particle " + particleEffect.name() + " for player " + player.getName());
            }
        }
    }

    private void removeGoldBlockTrails() {
        if (PLAYER_GOLD_BLOCK_TRAIL_INFO.isEmpty()) return;
        for (Map.Entry<Location, Material> goldTrails : PLAYER_GOLD_BLOCK_TRAIL_INFO.entrySet()) {
            Location location = goldTrails.getKey();
            int timeRemaining = location.getBlock().getMetadata("time").get(0).asInt();
            timeRemaining--;
            if (timeRemaining <= 0) {
                Material material = goldTrails.getValue();
                location.getBlock().setType(material);
                PLAYER_GOLD_BLOCK_TRAIL_INFO.remove(location);
            } else {
                location.getBlock().setMetadata("time", new FixedMetadataValue(DungeonRealms.getInstance(), timeRemaining));
            }
        }
    }

    private void spawnEntityParticleEffects() {
        if (ENTITY_PARTICLE_EFFECTS.isEmpty()) return;
        for (Entity entity : ENTITY_PARTICLE_EFFECTS.keySet()) {
            if (!entity.isAlive()) {
                ENTITY_PARTICLE_EFFECTS.remove(entity);
                continue;
            }
            float moveSpeed = 0.02F;
            ParticleAPI.ParticleEffect particleEffect = ENTITY_PARTICLE_EFFECTS.get(entity);
            if (particleEffect == ParticleAPI.ParticleEffect.RED_DUST || particleEffect == ParticleAPI.ParticleEffect.NOTE) {
                moveSpeed = -1F;
            }
            Location location = new Location(Bukkit.getWorlds().get(0), entity.locX, entity.locY, entity.locZ);
            try {
                ParticleAPI.sendParticleToLocation(particleEffect, location.add(0, 0.22, 0), (random.nextFloat()) - 0.4F, (random.nextFloat()) - 0.5F, (random.nextFloat()) - 0.5F, moveSpeed, 6);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.log.warning("[Donations] Could not spawn donation particle " + particleEffect.name() + " for entity " + entity.getName());
            }
        }
    }

    public boolean removeECashFromPlayer(Player player, int amount) {
        if (amount <= 0) {
            return true;
            //Someone done fucked up and made it remove a negative amount. Probably Chase.
        }
        int playerEcash = (int) DatabaseAPI.getInstance().getData(EnumData.ECASH, player.getUniqueId());
        if (playerEcash <= 0) {
            return false;
        }
        if (playerEcash - amount >= 0) {
            GamePlayer gamePlayer = GameAPI.getGamePlayer(player);
            if (gamePlayer == null) return false;
            gamePlayer.getPlayerStatistics().setEcashSpent(gamePlayer.getPlayerStatistics().getEcashSpent() + amount);
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.ECASH, (amount * -1), true);
            return true;
        } else {
            return false;
        }
    }
}
