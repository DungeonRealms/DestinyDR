package net.dungeonrealms.game.listener.inventory;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.PassiveDRChicken;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
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
import java.util.concurrent.ThreadLocalRandom;

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
        if (Metadata.PASSIVE.get(event.getEntity()).asBoolean() && event.getDamager() instanceof Player) {
            //Anger?
            angerLevels.put(event.getEntity(), 40);

            CraftLivingEntity ent = (CraftLivingEntity) event.getEntity();
            if (ent.getHandle() instanceof PassiveDRChicken) {
                PassiveDRChicken chicken = (PassiveDRChicken) ent.getHandle();
                //Target them.
                if (chicken.getGoalTarget() == null || ThreadLocalRandom.current().nextInt(2) == 0)
                    chicken.setGoalTarget(((CraftLivingEntity) event.getDamager()).getHandle(), EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY, true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() != null && event.getTarget() instanceof Player) {
            if (Metadata.PASSIVE.get(event.getEntity()).asBoolean()) {
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
