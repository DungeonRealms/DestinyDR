package net.dungeonrealms.donate;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ParticleAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by Kieran on 10/1/2015.
 */
public class DonationParticleEffects {

    private static DonationParticleEffects instance = null;

    public static DonationParticleEffects getInstance() {
        if (instance == null) {
            instance = new DonationParticleEffects();
        }
        return instance;
    }

    public static HashMap<Player, ParticleAPI.ParticleEffect> playerParticleEffects = new HashMap<>();

    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::spawnPlayerParticleEffects, 40L, 1L);
    }

    private void spawnPlayerParticleEffects() {
        Bukkit.getOnlinePlayers().stream().filter(playerParticleEffects::containsKey).forEach(player -> Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            try {
                ParticleAPI.sendParticleToLocation(playerParticleEffects.get(player), player.getLocation().add(0, 0.22, 0), (new Random().nextFloat()) - 0.4F, (new Random().nextFloat()) - 0.5F, (new Random().nextFloat()) - 0.5F, 0.02F, 7);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.log.warning("[Donations] [ASYNC] Could not spawn donation particle for player " + player.getName());
            }
        }, 0L));
    }
}
