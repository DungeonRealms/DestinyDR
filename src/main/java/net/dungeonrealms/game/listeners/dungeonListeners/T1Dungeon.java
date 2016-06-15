package net.dungeonrealms.game.listeners.dungeonListeners;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Created by Kieran Quigley (Proxying) on 15-Jun-16.
 */
public class T1Dungeon implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPyromancerDeath(EntityDeathEvent event) {
        if (!event.getEntity().getWorld().getName().contains("DUNGEON")) return;
        if (event.getEntity() instanceof Player) return;
        if (event.getEntity().hasMetadata("elite")) {
            if (event.getEntity().hasMetadata("customname")) {
                if (event.getEntity().getMetadata("customname").get(0).asString().contains("Pyromancer")) {
                    event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation().add(0, 2, 0), ItemManager.createItem(Material.GLOWSTONE_DUST, ChatColor.GREEN + "Magical Dust", new String[] {
                            ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "A strange substance that animates objects.", ChatColor.RED + "Dungeon Item"}));
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()-> {
                        event.getEntity().getWorld().getBlockAt(641, 55, -457).setType(Material.REDSTONE_TORCH_ON);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> event.getEntity().getWorld().getBlockAt(641, 55, -457).setType(Material.AIR), 20);
                    }, 20 * 5);
                    for (Player player : API.getNearbyPlayers(event.getEntity().getLocation(), 30)) {
                        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD + event.getEntity().getMetadata("customname").get(0).asString() + ChatColor.WHITE + "] "
                                + ChatColor.GREEN + "Talk about going out with a...blast.");
                    }
                    //TODO: Blow up door.
                }
            }
        }
    }
}
