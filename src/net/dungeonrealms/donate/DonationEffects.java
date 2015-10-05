package net.dungeonrealms.donate;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.MinecraftServer;
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
public class DonationEffects {

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

    public static HashMap<Player, ParticleAPI.ParticleEffect> playerParticleEffects = new HashMap<>();
    public static HashMap<Entity, ParticleAPI.ParticleEffect> entityParticleEffects = new HashMap<>();
    public static ConcurrentHashMap<Location, Material> playerGoldBlockTrailLocation = new ConcurrentHashMap<>();
    public static List<Player> playerGoldBlockTrail = new ArrayList<>();

    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::spawnPlayerParticleEffects, 40L, 1L);
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::spawnEntityParticleEffects, 40L, 1L);
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::removeGoldBlockTrails, 40L, 3L);
    }

    private void spawnPlayerParticleEffects() {
        Bukkit.getOnlinePlayers().stream().filter(playerParticleEffects::containsKey).forEach(player -> Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            try {
                ParticleAPI.sendParticleToLocation(playerParticleEffects.get(player), player.getLocation().add(0, 0.22, 0), (new Random().nextFloat()) - 0.4F, (new Random().nextFloat()) - 0.5F, (new Random().nextFloat()) - 0.5F, 0.02F, 6);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.log.warning("[Donations] [ASYNC] Could not spawn donation particle " + playerParticleEffects.get(player).name() + " for player " + player.getName());
            }
        }, 0L));
    }

    private void removeGoldBlockTrails() {
        for (Map.Entry<Location, Material> goldTrails : playerGoldBlockTrailLocation.entrySet()) {
            Location location = goldTrails.getKey();
            int timeRemaining = location.getBlock().getMetadata("time").get(0).asInt();
            timeRemaining--;
            if (timeRemaining <= 0) {
                Material material = goldTrails.getValue();
                location.getBlock().setType(material);
                playerGoldBlockTrailLocation.remove(location);
            } else {
                location.getBlock().setMetadata("time", new FixedMetadataValue(DungeonRealms.getInstance(), timeRemaining));
            }
        }
    }

    private void spawnEntityParticleEffects() {
        MinecraftServer.getServer().getWorld().entityList.stream().filter(entityParticleEffects::containsKey).forEach(entity -> Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            Location location = new Location(Bukkit.getWorlds().get(0), entity.locX, entity.locY, entity.locZ);
            try {
                ParticleAPI.sendParticleToLocation(entityParticleEffects.get(entity), location.add(0, 0.22, 0), (new Random().nextFloat()) - 0.4F, (new Random().nextFloat()) - 0.5F, (new Random().nextFloat()) - 0.5F, 0.02F, 6);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.log.warning("[Donations] [ASYNC] Could not spawn donation particle " + entityParticleEffects.get(entity).name() + " for entity " + entity.getName());
            }
        }, 0L));
    }
}
