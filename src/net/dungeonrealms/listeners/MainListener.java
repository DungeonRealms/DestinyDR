package net.dungeonrealms.listeners;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mechanics.WebAPI;
import net.dungeonrealms.mongo.DatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

/**
 * Created by Nick on 9/17/2015.
 */
public class MainListener implements Listener {

    /**
     * This event is used for the Database.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onAsyncJoin(AsyncPlayerPreLoginEvent event) {
        DatabaseAPI.getInstance().requestPlayer(event.getUniqueId());
    }

    /**
     * This event is the main event once the player has
     * actually entered the world! It is now safe to
     * do things to the player e.g BountifulAPI or
     * adding PotionEffects.. etc..
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (WebAPI.ANNOUNCEMENTS != null && WebAPI.ANNOUNCEMENTS.size() > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                for (Map.Entry<String, Integer> e : WebAPI.ANNOUNCEMENTS.entrySet()) {
                    BountifulAPI.sendTitle(player, 1, e.getValue(), 1, e.getKey().replace(":", ""), e.getKey().split(":")[0]);
                    try {
                        Thread.sleep(e.getValue());
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }, 0l);
        }
    }

    /**
     * Cancel spawning unless it's CUSTOM.
     * So we don't have RANDOM SHEEP. We have..
     * CUSTOM SHEEP. RAWR SHEEP EAT ME>.. AH RUN!
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;

        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }


}
