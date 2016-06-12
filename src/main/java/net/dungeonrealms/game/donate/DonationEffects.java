package net.dungeonrealms.game.donate;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    public ConcurrentHashMap<Entity, ParticleAPI.ParticleEffect> ENTITY_PARTICLE_EFFECTS = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Location, Material> PLAYER_GOLD_BLOCK_TRAIL_INFO = new ConcurrentHashMap<>();
    public List<Player> PLAYER_GOLD_BLOCK_TRAILS = new ArrayList<>();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
	public void startInitialization() {
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::spawnPlayerParticleEffects, 40L, 2L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::spawnEntityParticleEffects, 40L, 2L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::removeGoldBlockTrails, 40L, 4L);
    }

    @Override
    public void stopInvocation() {

    }

    private void spawnPlayerParticleEffects() {
        Bukkit.getOnlinePlayers().stream().filter(PLAYER_PARTICLE_EFFECTS::containsKey).forEach(player -> Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            float moveSpeed = 0.02F;
            if (PLAYER_PARTICLE_EFFECTS.get(player) == ParticleAPI.ParticleEffect.RED_DUST || PLAYER_PARTICLE_EFFECTS.get(player) == ParticleAPI.ParticleEffect.NOTE) {
                moveSpeed = -1F;
            }
            try {
                ParticleAPI.sendParticleToLocation(PLAYER_PARTICLE_EFFECTS.get(player), player.getLocation().add(0, 0.22, 0), (new Random().nextFloat()) - 0.4F, (new Random().nextFloat()) - 0.5F, (new Random().nextFloat()) - 0.5F, moveSpeed, 6);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.log.warning("[Donations] Could not spawn donation particle " + PLAYER_PARTICLE_EFFECTS.get(player).name() + " for player " + player.getName());
            }
        }, 0L));
    }

    private void removeGoldBlockTrails() {
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
        MinecraftServer.getServer().getWorld().entityList.stream().filter(ENTITY_PARTICLE_EFFECTS::containsKey).forEach(entity -> Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            float moveSpeed = 0.02F;
            if (ENTITY_PARTICLE_EFFECTS.get(entity) == ParticleAPI.ParticleEffect.RED_DUST || ENTITY_PARTICLE_EFFECTS.get(entity) == ParticleAPI.ParticleEffect.NOTE) {
                moveSpeed = -1F;
            }
            Location location = new Location(Bukkit.getWorlds().get(0), entity.locX, entity.locY, entity.locZ);
            try {
                ParticleAPI.sendParticleToLocation(ENTITY_PARTICLE_EFFECTS.get(entity), location.add(0, 0.22, 0), (new Random().nextFloat()) - 0.4F, (new Random().nextFloat()) - 0.5F, (new Random().nextFloat()) - 0.5F, moveSpeed, 6);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.log.warning("[Donations] Could not spawn donation particle " + ENTITY_PARTICLE_EFFECTS.get(entity).name() + " for entity " + entity.getName());
            }
        }, 0L));
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
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, EnumData.ECASH, (amount * -1), true);
            return true;
        } else {
            return false;
        }
    }
}
