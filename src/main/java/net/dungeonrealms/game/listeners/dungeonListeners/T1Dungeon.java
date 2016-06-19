package net.dungeonrealms.game.listeners.dungeonListeners;

import net.dungeonrealms.API;
import net.dungeonrealms.game.mechanics.DungeonManager;
import net.dungeonrealms.game.mechanics.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 15-Jun-16.
 */
public class T1Dungeon implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNamedMobDeath(EntityDeathEvent event) {
        if (!event.getEntity().getWorld().getName().contains("DUNGEON")) return;
        if (event.getEntity() instanceof Player) return;
        if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()) == null) return;
        if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()).getType() != DungeonManager.DungeonType.BANDIT_TROVE) return;
        if (event.getEntity().hasMetadata("elite")) {
            if (event.getEntity().hasMetadata("customname")) {
                String name = ChatColor.stripColor(event.getEntity().getMetadata("customname").get(0).asString());
                if (name.equalsIgnoreCase("Mad Bandit Pyromancer")) {
                    ItemStack key = ItemManager.createItem(Material.GLOWSTONE_DUST, ChatColor.GREEN + "Magical Dust", new String[] {
                            ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "A strange substance that animates objects.", ChatColor.RED + "Dungeon Item"});
                    if (event.getEntity().getKiller() != null) {
                        event.getEntity().getKiller().getInventory().addItem(key);
                    } else {
                        event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation().add(0, 1, 0), key);
                    }
                    for (Player player : API.getNearbyPlayers(event.getEntity().getLocation(), 30)) {
                        player.sendMessage(ChatColor.RED + event.getEntity().getMetadata("customname").get(0).asString() + ChatColor.WHITE + ": " + ChatColor.WHITE + "Talk about going out with a...blast.");
                    }
                }
            }
        }
    }
}
