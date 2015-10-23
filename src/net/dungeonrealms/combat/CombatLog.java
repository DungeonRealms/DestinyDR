package net.dungeonrealms.combat;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nick on 8/29/2015.
 */
public class CombatLog implements GenericMechanic{

    private static CombatLog instance = null;

    public static CombatLog getInstance() {
        if (instance == null) {
            instance = new CombatLog();
        }
        return instance;

    }

    public static ConcurrentHashMap<Player, Integer> COMBAT = new ConcurrentHashMap<>();

    public static boolean isInCombat(Player player) {
        return COMBAT.containsKey(player);
    }

    public static void updateCombat(Player player) {
        if (isInCombat(player)) {
            COMBAT.put(player, 10);
        }
    }

    public static void addToCombat(Player player) {
        if (!isInCombat(player)) {
            COMBAT.put(player, 10);
            player.sendMessage(ChatColor.RED + "You are now in combat! (10) Seconds!");
        }
    }

    public static void removeFromCombat(Player player) {
        if (isInCombat(player)) {
            COMBAT.remove(player);
            player.sendMessage(ChatColor.GREEN + "You are no longer in combat!");
        }
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (Map.Entry<Player, Integer> e : COMBAT.entrySet()) {
                if (e.getValue() <= 0) {
                    removeFromCombat(e.getKey());
                } else {
                    COMBAT.put(e.getKey(), (e.getValue() - 1));
                }
            }
        }, 0, 20l);
    }

    @Override
    public void stopInvocation() {

    }
}