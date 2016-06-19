package net.dungeonrealms.game.listeners.dungeonListeners;

import net.dungeonrealms.game.mechanics.DungeonManager;
import net.dungeonrealms.game.mechanics.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by Kieran Quigley (Proxying) on 15-Jun-16.
 */
public class T3Dungeon implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onNamedMobDeath(EntityDeathEvent event) {
        if (!event.getEntity().getWorld().getName().contains("DUNGEON")) return;
        if (event.getEntity() instanceof Player) return;
        if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()) == null) return;
        if (DungeonManager.getInstance().getDungeon(event.getEntity().getWorld()).getType() != DungeonManager.DungeonType.VARENGLADE) return;
        DungeonManager.DungeonObject dungeonObject = DungeonManager.getInstance().getDungeon(event.getEntity().getWorld());
        if (dungeonObject.keysDropped <= 10) {
            if (new Random().nextInt(20) <= 14) {
                ItemStack key = ItemManager.createItem(Material.TRIPWIRE_HOOK, ChatColor.LIGHT_PURPLE + "A mystical key", new String[]{
                        ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "One of four mysterious keys.", ChatColor.RED + "Dungeon Item"});
                if (event.getEntity().getKiller() != null) {
                    event.getEntity().getKiller().getInventory().addItem(key);
                } else {
                    event.getEntity().getWorld().dropItemNaturally(new Location(event.getEntity().getWorld(), 36, 54, -4), key);
                }
                dungeonObject.keysDropped = dungeonObject.keysDropped + 1;
            }
        }
    }
}
