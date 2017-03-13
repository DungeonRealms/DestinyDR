package net.dungeonrealms.game.listener.inventory;

import net.dungeonrealms.DungeonRealms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PassiveEntityListener implements Listener {

    //Weak incase something happens to the entity.
    private Map<Entity, Integer> angerLevels = new ConcurrentHashMap<>();

    public PassiveEntityListener() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> angerLevels.forEach((ent, timer) -> {
            if (ent.isDead() || !ent.isValid()) {
                this.angerLevels.remove(ent);
                return;
            }
            Creature creature = (Creature) ent;
            if (!creature.isDead()) {
                if (timer <= 0) {
                    //Anger is out, stop attacking since noone has attacked clearly?
                    creature.setTarget(null);
                    this.angerLevels.remove(ent);
                    return;
                }

                angerLevels.put(ent, --timer);
            }
        }), 20 * 10, 20);
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity().hasMetadata("passive") && event.getDamager() instanceof Player) {
            //Anger?
            angerLevels.put(event.getEntity(), 40);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() != null && event.getTarget() instanceof Player) {
            if (event.getEntity().hasMetadata("passive")) {
                Integer angerLevel = angerLevels.get(event.getEntity());
                if (angerLevel != null && angerLevel > 0)
                    return;

                event.setCancelled(true);
                event.setTarget(null);

                Creature creature = (Creature) event.getEntity();
                creature.setTarget(null);

            }
        }

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() != null && event.getTarget() instanceof Player) {
            if (event.getEntity().hasMetadata("passive")) {
                Integer angerLevel = angerLevels.get(event.getEntity());
                if (angerLevel != null && angerLevel > 0)
                    return;

                event.setCancelled(true);
                event.setTarget(null);

                Creature creature = (Creature) event.getEntity();
                creature.setTarget(null);

            }
        }

    }
}
