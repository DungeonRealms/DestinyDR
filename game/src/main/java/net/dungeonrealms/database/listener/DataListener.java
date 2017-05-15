package net.dungeonrealms.database.listener;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.game.database.sql.UUIDName;
import net.dungeonrealms.database.PlayerWrapper;

import net.dungeonrealms.game.mechanic.dungeons.Dungeon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

 /**
 * Created by Rar349 on 4/13/2017.
 */
public class DataListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        PlayerWrapper wrapper = new PlayerWrapper(event.getUniqueId());
        PlayerWrapper.setWrapper(event.getUniqueId(), wrapper);
        wrapper.loadPunishment(false, null);

        if(wrapper.isBanned()) {
            //This will never get called. It will catch them in the lobby instead.
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED.toString() + "The account " + ChatColor.BOLD.toString() + event.getName() + ChatColor.RED.toString()

                    + " is banned. Your ban expires in " + ChatColor.UNDERLINE.toString() + wrapper.getTimeWhenBanExpires() + "." +
                    "\n\n" + ChatColor.RED.toString()
                    + "You were banned for:\n" + ChatColor.UNDERLINE.toString() + wrapper.getBanReason());
            return;
        }
        wrapper.loadData(false);

        //Update their username to be up2date.
        wrapper.setUsername(event.getName());
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
//        DatabaseAPI.getInstance().requestPlayer(event.getUniqueId(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerJoinEvent event) {
        if(!event.getPlayer().isOnline())return;
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(event.getPlayer().getUniqueId());
        if(wrapper == null) return;
        wrapper.setLastLogin(System.currentTimeMillis());
        wrapper.loadPlayerAfterLogin(event.getPlayer());
        //Overwrite whatever we have in there.
        SQLDatabaseAPI.getInstance().getAccountIdNames().put(wrapper.getAccountID(), new UUIDName(event.getPlayer().getUniqueId(), event.getPlayer().getName()));
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> event.getPlayer().setResourcePack(Constants.RESOURCE_PACK));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        onDC(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        onDC(event.getPlayer());
    }

    private void onDC(Player player) {
    	PlayerWrapper wrapper = PlayerWrapper.getWrapper(player);
        if(wrapper == null)
        	return;
        //We need to remove this instance of the player after they have left the server.. Keep that object for some more use tho.
        wrapper.setPlayer(null);
//        wrapper.saveData(true, event.getPlayer(), true, (newWrapper) -> newWrapper.setPlayingStatus(false));
    }
}
