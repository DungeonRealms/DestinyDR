package net.dungeonrealms.database.listener;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

 /**
 * Created by Rar349 on 4/13/2017.
 */
public class DataListener implements Listener {

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        PlayerWrapper wrapper = new PlayerWrapper(event.getUniqueId());
        PlayerWrapper.setWrapper(event.getUniqueId(), wrapper);
        wrapper.loadPunishment(false);

        if(wrapper.isBanned()) {
            //This will never get called. It will catch them in the lobby instead.
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED.toString() + "The account " + ChatColor.BOLD.toString() + event.getName() + ChatColor.RED.toString()

                    + " is banned. Your ban expires in " + ChatColor.UNDERLINE.toString() + wrapper.getTimeWhenBanExpires() + "." +
                    "\n\n" + ChatColor.RED.toString()
                    + "You were banned for:\n" + ChatColor.UNDERLINE.toString() + wrapper.getBanReason());
            return;
        }
        wrapper.loadData(false);

        if(wrapper.isPlaying()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.YELLOW.toString() + "The account " + ChatColor.BOLD.toString() + event.getName() + ChatColor.YELLOW.toString()

                    + " is already logged in on " + ChatColor.UNDERLINE.toString() + wrapper.getShardPlayingOn() + "." +
                    "\n\n" + ChatColor.GRAY.toString()
                    + "If you have just recently changed servers, your character data is being synced -- " + ChatColor.UNDERLINE.toString()
                    + "wait a few seconds" + ChatColor.GRAY.toString() + " before reconnecting.");
            return;
        }

        if (DungeonRealms.getInstance().getLoggingOut().contains(event.getName())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Please wait while your data syncs.");
            DungeonRealms.getInstance().getLoggingOut().remove(event.getName());
            return;
        }

        DungeonRealms.getInstance().getLoggingIn().add(event.getUniqueId());

        // REQUEST PLAYER'S DATA ASYNC //
        DatabaseAPI.getInstance().requestPlayer(event.getUniqueId(), false);
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(event.getPlayer().getUniqueId());
        if(wrapper == null) return;
        wrapper.loadPlayerInventory(event.getPlayer());
        wrapper.loadPlayerArmor(event.getPlayer());
    }

    @EventHandler()
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(event.getPlayer().getUniqueId());
        if(wrapper == null) return;
//        wrapper.saveData(true, event.getPlayer(), true, (newWrapper) -> newWrapper.setPlayingStatus(false));
    }
}
