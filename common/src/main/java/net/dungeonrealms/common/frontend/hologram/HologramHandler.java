package net.dungeonrealms.common.frontend.hologram;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Created by Evoltr on 11/20/2016.
 */
public class HologramHandler implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        HologramManager.getInstance().handlePlayerQuit(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getWorld() == event.getTo().getWorld() &&
                event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ())
            return;

        Player player = event.getPlayer();
        if (player.getHealth() <= 0.0D) return;

        Chunk oldChunk = event.getFrom().getChunk();
        Chunk newChunk = event.getTo().getChunk();

        if (oldChunk.getWorld() != newChunk.getWorld() || oldChunk.getX() != newChunk.getX() || oldChunk.getZ() != newChunk.getZ()) {
            HologramManager.getInstance().updatePlayerView(player);
        }

    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        HologramManager.getInstance().updatePlayerView(event.getPlayer());
    }
}
