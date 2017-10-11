package net.dungeonrealms.game.quests.compass;

import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.title.TitleAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.inventivetalent.bossbar.BossBarAPI;

/**
 * Created by Rar349 on 8/8/2017.
 */
public class CompassListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        CompassManager manager = CompassManager.getManager(event.getPlayer());
        if(manager == null) return;
        if(manager.shouldShowBar())HealthHandler.updateBossBar(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        CompassManager.registerManager(new CompassManager(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        CompassManager.unregisterManager(event.getPlayer());
    }

}
