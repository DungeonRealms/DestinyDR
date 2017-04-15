package net.dungeonrealms.database.listener;

import net.dungeonrealms.database.PlayerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by Rar349 on 4/13/2017.
 */
public class DataListener implements Listener {

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        PlayerWrapper wrapper = new PlayerWrapper(event.getUniqueId());
        PlayerWrapper.setWrapper(event.getUniqueId(), wrapper);
        wrapper.loadData(false);

        if(wrapper.isPlaying()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.YELLOW.toString() + "The account " + ChatColor.BOLD.toString() + event.getName() + ChatColor.YELLOW.toString()

                    + " is already logged in on " + ChatColor.UNDERLINE.toString() + wrapper.getShardPlayingOn() + "." +
                    "\n\n" + ChatColor.GRAY.toString()
                    + "If you have just recently changed servers, your character data is being synced -- " + ChatColor.UNDERLINE.toString()
                    + "wait a few seconds" + ChatColor.GRAY.toString() + " before reconnecting.");
            return;
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(event.getPlayer().getUniqueId());
        if(wrapper == null) return;
        wrapper.loadPlayerInventory(event.getPlayer());
        wrapper.loadPlayerArmor(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(event.getPlayer().getUniqueId());
        if(wrapper == null) return;
        wrapper.saveData(true, (wrap) -> {
            wrap.setPlaying(false);
        });

    }
}
