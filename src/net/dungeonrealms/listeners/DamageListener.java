package net.dungeonrealms.listeners;

import net.dungeonrealms.API;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Created by Nick on 9/17/2015.
 */
public class DamageListener implements Listener {


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityDamage(EntityDamageEvent event) {
    }

}
