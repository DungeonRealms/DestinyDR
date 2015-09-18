package net.dungeonrealms.combat;

import net.dungeonrealms.DungeonRealms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Nick on 8/29/2015.
 */
public class CombatLog {

    static CombatLog instance = null;

    public static CombatLog getInstance() {
        if (instance == null) {
            instance = new CombatLog();
        }
        return instance;

    }

    public static HashMap<UUID, Integer> COMBAT = new HashMap<>();

    public static boolean isInCombat(UUID uuid) {
        return COMBAT.containsKey(uuid);
    }

    public static void updateCombat(UUID uuid) {
        if (isInCombat(uuid)) {
            COMBAT.put(uuid, 10);
        }
    }

    public static void addToCombat(UUID uuid) {
        if (!isInCombat(uuid)) {
            COMBAT.put(uuid, 10);
            if (Bukkit.getPlayer(uuid) != null) {
                Bukkit.getPlayer(uuid).sendMessage(new String[]{
                        "",
                        ChatColor.RED + "You are now in combat! (10) Seconds!",
                        ""
                });
            }
        }
    }

    public static void removeFromCombat(UUID uuid) {
        if (isInCombat(uuid)) {
            COMBAT.remove(uuid);
            if (Bukkit.getPlayer(uuid) != null) {
                Bukkit.getPlayer(uuid).sendMessage(new String[]{
                        "",
                        ChatColor.GREEN + "You are no longer in combat!",
                        ""
                });
            }
        }
    }

    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (Map.Entry<UUID, Integer> e : COMBAT.entrySet()) {
                if (e.getValue() == 0) {
                    removeFromCombat(e.getKey());
                }
                COMBAT.put(e.getKey(), (e.getValue() - 1));
            }
        }, 0, 20l);
    }
}