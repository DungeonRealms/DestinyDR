package net.dungeonrealms.old.game.mastery;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Kieran Quigley (Proxying) on 08-Jul-16.
 */
public class DamageTracker {

    @Getter
    private HashMap<UUID, Double> damagers = new HashMap<>();
    @Getter
    private UUID monsterUUID;

    public DamageTracker(UUID monsterUUID) {
        this.monsterUUID = monsterUUID;
    }

    public void addPlayerDamage(Player player, double damageToAdd) {
        if (!damagers.containsKey(player.getUniqueId())) {
            damagers.put(player.getUniqueId(), damageToAdd);
        } else {
            damagers.put(player.getUniqueId(), damagers.get(player.getUniqueId()) + damageToAdd);
        }
    }

    public void removeDamager(Player player) {
        if (damagers.containsKey(player.getUniqueId())) {
            damagers.remove(player.getUniqueId());
        }
    }

    public Player findHighestDamageDealer() {
        double highestDamage = 0;
        UUID damager = null;
        for (Map.Entry<UUID, Double> entry : damagers.entrySet()) {
            double damage = entry.getValue();
            if (damage > highestDamage) {
                highestDamage = damage;
                damager = entry.getKey();
            }
        }
        if (damager == null) {
            return null;
        } else {
            return Bukkit.getPlayer(damager);
        }
    }
}
